package org.hibernate.envers.configuration;

/**
 * Configuration property names.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public abstract class EnversEnvironment {
	// Global configuration.
	public static final String REVISION_ON_COLLECTION_CHANGE = "org.hibernate.envers.revision_on_collection_change";
	public static final String DO_NOT_AUDIT_OPTIMISTIC_LOCKING_FIELD = "org.hibernate.envers.do_not_audit_optimistic_locking_field";
	public static final String STORE_DATA_AT_DELETE = "org.hibernate.envers.store_data_at_delete";
	public static final String DEFAULT_SCHEMA = "org.hibernate.envers.default_schema";
	public static final String DEFAULT_CATALOG = "org.hibernate.envers.default_catalog";
	public static final String TRACK_ENTITIES_CHANGED_IN_REVISION = "org.hibernate.envers.track_entities_changed_in_revision";
	public static final String USE_REVISION_ENTITY_WITH_NATIVE_ID = "org.hibernate.envers.use_revision_entity_with_native_id";
	public static final String GLOBAL_WITH_MODIFIED_FLAG_PROPERTY = "org.hibernate.envers.global_with_modified_flag";
	public static final String MODIFIED_FLAG_SUFFIX_PROPERTY = "org.hibernate.envers.modified_flag_suffix";
	public static final String REVISION_LISTENER = "org.hibernate.envers.revision_listener";

	// Entities configuration.
	public final String AUDIT_TABLE_PREFIX = "org.hibernate.envers.audit_table_prefix";
	public final String AUDIT_TABLE_SUFFIX = "org.hibernate.envers.audit_table_suffix";
	public final String AUDIT_STRATEGY = "org.hibernate.envers.audit_strategy";
	public final String REVISION_FIELD_NAME = "org.hibernate.envers.revision_field_name";
	public final String REVISION_TYPE_FIELD_NAME = "org.hibernate.envers.revision_type_field_name";
	public final String AUDIT_STRATEGY_VALIDITY_END_REV_FIELD_NAME = "org.hibernate.envers.audit_strategy_validity_end_rev_field_name";
	public final String AUDIT_STRATEGY_VALIDITY_STORE_REVEND_TIMESTAMP = "org.hibernate.envers.audit_strategy_validity_store_revend_timestamp";
	public final String AUDIT_STRATEGY_VALIDITY_REVEND_TIMESTAMP_FIELD_NAME = "org.hibernate.envers.audit_strategy_validity_revend_timestamp_field_name";
}
