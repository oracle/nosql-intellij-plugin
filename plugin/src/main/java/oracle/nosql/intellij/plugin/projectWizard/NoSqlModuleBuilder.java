/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.projectWizard;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import oracle.nosql.intellij.plugin.common.NoSqlIcons;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * ModuleBuilder is a hook for creating new modules in IDEA.
 * This is responsible for New->Project->Oracle NOSQL examples project wizard.
 * See <a href="https://www.jetbrains.org/intellij/sdk/docs/reference_guide/project_wizard.html">project wizard</a>.
 *
 * @author amsundar
 */
public class NoSqlModuleBuilder extends JavaModuleBuilder implements ModuleBuilderListener {
    private static final String JAVA = ".java";
    private String nosqlSDKPath = null;
    void setNosqlSDKPath(String nosqlSDKPath) {
        this.nosqlSDKPath = nosqlSDKPath;
    }

    /*@Override
    public ModuleType getModuleType() {
        return NoSqlModuleType.getInstance();
    }*/

    public NoSqlModuleBuilder(){ addListener(this);}

    @Nullable
    @Override
    public String getBuilderId() {
        return OracleNoSqlBundle.message("oracle.nosql.module.bundle.id");
    }

    @Override
    public Icon getNodeIcon() {
        return NoSqlIcons.ORACLE_LOGO;
    }

    //this creates project structure and copies example java files from SDK path
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        @NonNls final String path = getContentEntryPath() + File.separator + "src";
        new File(path).mkdirs();
        addSourcePath(Pair.create(path,""));
        final String packagePath = path;
        File packageFile = new File(packagePath);
        packageFile.mkdirs();
        super.setupRootModel(rootModel);

        //add nosql libs to module classpath
        LibraryTable libraryTable = rootModel.getModuleLibraryTable();
        Library library = libraryTable.createLibrary();
        Library.ModifiableModel modifiableModel = library.getModifiableModel();
        modifiableModel.addJarDirectory(VfsUtil.getUrlForLibraryRoot(new File(nosqlSDKPath)),true,OrderRootType.CLASSES);

        //copy java examples from sdk
        String examplePath = nosqlSDKPath+OracleNoSqlBundle.message("oracle.nosql.sdk.example.path");
        File f = new File(examplePath);
        FileFilter filter = pathname -> pathname.getName().endsWith(JAVA);
        try {
            FileUtil.copyDir(f,packageFile,filter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        modifiableModel.commit();
    }

    @Override
    public String getPresentableName() {
        return OracleNoSqlBundle.message("oracle.nosql.project.template.name");
    }
    @Override
    public String getGroupName() {
        return OracleNoSqlBundle.message("oracle.nosql.settings.main.name");
    }

    /**
     *Add NoSQL SDK selection step in new project wizard.
     */
    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{
                new SdkSelectionModuleWizardStep(this)
                };
    }

    @Override
    public void moduleCreated(@NotNull Module module) {

    }
}
