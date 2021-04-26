/*
 * This file is generated by jOOQ.
 */
package eu.fasten.core.data.metadatadb.codegen.tables.records;


import eu.fasten.core.data.metadatadb.codegen.tables.ModuleContents;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ModuleContentsRecord extends TableRecordImpl<ModuleContentsRecord> implements Record2<Long, Long> {

    private static final long serialVersionUID = 2046392239;

    /**
     * Setter for <code>public.module_contents.module_id</code>.
     */
    public void setModuleId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.module_contents.module_id</code>.
     */
    public Long getModuleId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.module_contents.file_id</code>.
     */
    public void setFileId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.module_contents.file_id</code>.
     */
    public Long getFileId() {
        return (Long) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Long, Long> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return ModuleContents.MODULE_CONTENTS.MODULE_ID;
    }

    @Override
    public Field<Long> field2() {
        return ModuleContents.MODULE_CONTENTS.FILE_ID;
    }

    @Override
    public Long component1() {
        return getModuleId();
    }

    @Override
    public Long component2() {
        return getFileId();
    }

    @Override
    public Long value1() {
        return getModuleId();
    }

    @Override
    public Long value2() {
        return getFileId();
    }

    @Override
    public ModuleContentsRecord value1(Long value) {
        setModuleId(value);
        return this;
    }

    @Override
    public ModuleContentsRecord value2(Long value) {
        setFileId(value);
        return this;
    }

    @Override
    public ModuleContentsRecord values(Long value1, Long value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ModuleContentsRecord
     */
    public ModuleContentsRecord() {
        super(ModuleContents.MODULE_CONTENTS);
    }

    /**
     * Create a detached, initialised ModuleContentsRecord
     */
    public ModuleContentsRecord(Long moduleId, Long fileId) {
        super(ModuleContents.MODULE_CONTENTS);

        set(0, moduleId);
        set(1, fileId);
    }
}
