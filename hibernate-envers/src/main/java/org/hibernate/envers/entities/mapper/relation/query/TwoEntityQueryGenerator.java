/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.entities.mapper.relation.query;

import java.util.Collections;

import org.hibernate.envers.configuration.AuditEntitiesConfiguration;
import org.hibernate.envers.configuration.GlobalConfiguration;
import org.hibernate.envers.entities.mapper.relation.MiddleComponentData;
import org.hibernate.envers.entities.mapper.relation.MiddleIdData;
import org.hibernate.envers.strategy.AuditStrategy;
import org.hibernate.envers.tools.query.Parameters;
import org.hibernate.envers.tools.query.QueryBuilder;

import static org.hibernate.envers.entities.mapper.relation.query.QueryConstants.DEL_REVISION_TYPE_PARAMETER;
import static org.hibernate.envers.entities.mapper.relation.query.QueryConstants.MIDDLE_ENTITY_ALIAS;
import static org.hibernate.envers.entities.mapper.relation.query.QueryConstants.REFERENCED_ENTITY_ALIAS;
import static org.hibernate.envers.entities.mapper.relation.query.QueryConstants.REFERENCED_ENTITY_ALIAS_DEF_AUD_STR;
import static org.hibernate.envers.entities.mapper.relation.query.QueryConstants.REVISION_PARAMETER;

/**
 * Selects data from a relation middle-table and a related versions entity.
 *
 * @author Adam Warski (adam at warski dot org)
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public final class TwoEntityQueryGenerator extends AbstractRelationQueryGenerator {
	private final String queryString;
	private final String queryRemovedString;

	public TwoEntityQueryGenerator(GlobalConfiguration globalCfg, AuditEntitiesConfiguration verEntCfg,
								   AuditStrategy auditStrategy, String versionsMiddleEntityName,
								   MiddleIdData referencingIdData, MiddleIdData referencedIdData,
								   boolean revisionTypeInId, MiddleComponentData... componentData) {
		super( verEntCfg, referencingIdData, revisionTypeInId );

		/*
		 * The query that we need to create:
		 *   SELECT new list(ee, e) FROM versionsReferencedEntity e, middleEntity ee
		 *   WHERE
		 * (entities referenced by the middle table; id_ref_ed = id of the referenced entity)
		 *     ee.id_ref_ed = e.id_ref_ed AND
		 * (only entities referenced by the association; id_ref_ing = id of the referencing entity)
		 *     ee.id_ref_ing = :id_ref_ing AND
		 *
		 * (selecting e entities at revision :revision)
		 *   --> for DefaultAuditStrategy:
		 *     e.revision = (SELECT max(e2.revision) FROM versionsReferencedEntity e2
		 *       WHERE e2.revision <= :revision AND e2.id = e.id)
		 *
		 *   --> for ValidityAuditStrategy:
		 *     e.revision <= :revision and (e.endRevision > :revision or e.endRevision is null)
		 *
		 *     AND
		 *
		 * (the association at revision :revision)
		 *   --> for DefaultAuditStrategy:
		 *     ee.revision = (SELECT max(ee2.revision) FROM middleEntity ee2
		 *       WHERE ee2.revision <= :revision AND ee2.originalId.* = ee.originalId.*)
		 *
		 *   --> for ValidityAuditStrategy:
		 *     ee.revision <= :revision and (ee.endRevision > :revision or ee.endRevision is null)
		 *
		 * (only non-deleted entities and associations)
		 *     ee.revision_type != DEL AND
		 *     e.revision_type != DEL
		 */
		final QueryBuilder commonPart = commonQueryPart( referencedIdData, versionsMiddleEntityName, verEntCfg.getOriginalIdPropName() );
		final QueryBuilder validQuery = commonPart.deepCopy();
		final QueryBuilder removedQuery = commonPart.deepCopy();
		createValidDataRestrictions(
				globalCfg, verEntCfg, auditStrategy, referencedIdData, versionsMiddleEntityName, validQuery,
				validQuery.getRootParameters(), componentData
		);
		createValidAndRemovedDataRestrictions(
				globalCfg, verEntCfg, auditStrategy, referencedIdData, versionsMiddleEntityName,
				removedQuery, componentData
		);

		queryString = queryToString( validQuery, Collections.<String, Object>emptyMap() );
		queryRemovedString = queryToString( removedQuery, Collections.<String, Object>emptyMap() );
	}

	/**
	 * Compute common part for both queries.
	 */
	private QueryBuilder commonQueryPart(MiddleIdData referencedIdData, String versionsMiddleEntityName,
										 String originalIdPropertyName) {
		final String eeOriginalIdPropertyPath = MIDDLE_ENTITY_ALIAS + "." + originalIdPropertyName;
		// SELECT new list(ee) FROM middleEntity ee
		QueryBuilder qb = new QueryBuilder( versionsMiddleEntityName, MIDDLE_ENTITY_ALIAS );
		qb.addFrom( referencedIdData.getAuditEntityName(), REFERENCED_ENTITY_ALIAS );
		qb.addProjection( "new list", MIDDLE_ENTITY_ALIAS + ", " + REFERENCED_ENTITY_ALIAS, false, false );
		// WHERE
		final Parameters rootParameters = qb.getRootParameters();
		// ee.id_ref_ed = e.id_ref_ed
		referencedIdData.getPrefixedMapper().addIdsEqualToQuery(
				rootParameters, eeOriginalIdPropertyPath, referencedIdData.getOriginalMapper(),
				REFERENCED_ENTITY_ALIAS + "." + originalIdPropertyName
		);
		// ee.originalId.id_ref_ing = :id_ref_ing
		referencingIdData.getPrefixedMapper().addNamedIdEqualsToQuery( rootParameters, originalIdPropertyName, true );
		return qb;
	}

	/**
	 * Creates query restrictions used to retrieve only actual data.
	 */
	private void createValidDataRestrictions(GlobalConfiguration globalCfg, AuditEntitiesConfiguration verEntCfg,
											 AuditStrategy auditStrategy, MiddleIdData referencedIdData,
											 String versionsMiddleEntityName, QueryBuilder qb, Parameters rootParameters,
											 MiddleComponentData... componentData) {
		final String revisionPropertyPath = verEntCfg.getRevisionNumberPath();
		final String originalIdPropertyName = verEntCfg.getOriginalIdPropName();
		final String eeOriginalIdPropertyPath = MIDDLE_ENTITY_ALIAS + "." + originalIdPropertyName;
		// (selecting e entities at revision :revision)
		// --> based on auditStrategy (see above)
		auditStrategy.addEntityAtRevisionRestriction(
				globalCfg, qb, rootParameters, REFERENCED_ENTITY_ALIAS + "." + revisionPropertyPath,
				REFERENCED_ENTITY_ALIAS + "." + verEntCfg.getRevisionEndFieldName(), false, referencedIdData,
				revisionPropertyPath, originalIdPropertyName, REFERENCED_ENTITY_ALIAS, REFERENCED_ENTITY_ALIAS_DEF_AUD_STR
		);
		// (with ee association at revision :revision)
		// --> based on auditStrategy (see above)
		auditStrategy.addAssociationAtRevisionRestriction( qb, rootParameters, revisionPropertyPath,
				verEntCfg.getRevisionEndFieldName(), true, referencingIdData, versionsMiddleEntityName,
				eeOriginalIdPropertyPath, revisionPropertyPath, originalIdPropertyName, MIDDLE_ENTITY_ALIAS,
				componentData
		);
		final String revisionTypePropName = getRevisionTypePath();
		// ee.revision_type != DEL
		rootParameters.addWhereWithNamedParam( revisionTypePropName, "!=", DEL_REVISION_TYPE_PARAMETER );
		// e.revision_type != DEL
		rootParameters.addWhereWithNamedParam( REFERENCED_ENTITY_ALIAS + "." + revisionTypePropName, false, "!=", DEL_REVISION_TYPE_PARAMETER );
	}

	/**
	 * Create query restrictions used to retrieve actual data and deletions that took place at exactly given revision.
	 */
	private void createValidAndRemovedDataRestrictions(GlobalConfiguration globalCfg, AuditEntitiesConfiguration verEntCfg,
													   AuditStrategy auditStrategy, MiddleIdData referencedIdData,
													   String versionsMiddleEntityName, QueryBuilder remQb,
													   MiddleComponentData... componentData) {
		final Parameters disjoint = remQb.getRootParameters().addSubParameters( "or" );
		final Parameters valid = disjoint.addSubParameters( "and" ); // Restrictions to match all valid rows.
		final Parameters removed = disjoint.addSubParameters( "and" ); // Restrictions to match all rows deleted at exactly given revision.
		createValidDataRestrictions( globalCfg, verEntCfg, auditStrategy, referencedIdData, versionsMiddleEntityName, remQb, valid, componentData );
		// ee.revision = :revision
		removed.addWhereWithNamedParam( MIDDLE_ENTITY_ALIAS + "." + verEntCfg.getRevisionNumberPath(), false, "=", REVISION_PARAMETER );
		// ee.revision_type = DEL
		removed.addWhereWithNamedParam( getRevisionTypePath(), "=", DEL_REVISION_TYPE_PARAMETER );
		// e.revision_type = DEL
		removed.addWhereWithNamedParam( REFERENCED_ENTITY_ALIAS + "." + getRevisionTypePath(), false, "=", DEL_REVISION_TYPE_PARAMETER );
	}

	@Override
	protected String getQueryString() {
		return queryString;
	}

	@Override
	protected String getQueryRemovedString() {
		return queryRemovedString;
	}
}
