/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.projectWizard;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import oracle.nosql.intellij.plugin.common.NoSqlIcons;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * New module type for Oracle NOSQL.
 * See <a href="https://www.jetbrains.org/intellij/sdk/docs/reference_guide/project_wizard.html">project wizard</a>.
 *
 * @author amsundar
 */
public class NoSqlModuleType  extends ModuleType<NoSqlModuleBuilder> {
    private static final String ID = OracleNoSqlBundle.message("oracle.nosql.module.bundle.id");
    // --Commented out by Inspection (14-Aug-19 4:12 PM):private static final NoSqlModuleType INSTANCE = new NoSqlModuleType();

    public NoSqlModuleType() {
        super(ID);
    }
    public static NoSqlModuleType getInstance() {
        return (NoSqlModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public NoSqlModuleBuilder createModuleBuilder() {
        return new NoSqlModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return OracleNoSqlBundle.message("oracle.nosql.settings.main.name");
    }

    @NotNull
    @Override
    public String getDescription() {
        return OracleNoSqlBundle.message("oracle.nosql.project.template.description");
    }

    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return NoSqlIcons.ORACLE_LOGO;
    }

}
