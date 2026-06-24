package org.fool.framework.dbmanage;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkDataBaseFactory {
    private static final String DUPLICATE_CREATE_MESSAGE = "不能创建名称，年度相同的帐套";
    private static final String DUPLICATE_SAVE_MESSAGE = "无法保存登录设置，保存会与现有登录冲突";

    private final WorkingDatabaseRepository workingDatabaseRepository;

    public WorkDataBaseFactory(WorkingDatabaseRepository workingDatabaseRepository) {
        this.workingDatabaseRepository = workingDatabaseRepository;
    }

    public List<WorkingDatabase> all(String appName) {
        return workingDatabaseRepository.findByAppName(appName);
    }

    public List<WorkingDatabase> allList() {
        return workingDatabaseRepository.findAll();
    }

    public void create(WorkingDatabase database) {
        if (workingDatabaseRepository.findByNameAndYear(database.getName(), database.getYear()) != null) {
            throw new IllegalStateException(DUPLICATE_CREATE_MESSAGE);
        }
        database.setCode(nextCode(workingDatabaseRepository.findMaxCode()));
        encryptPassword(database);
        workingDatabaseRepository.insert(database);
    }

    public void save(WorkingDatabase database) {
        if (workingDatabaseRepository.findByNameAndYearExceptCode(
                database.getName(),
                database.getYear(),
                database.getCode()) != null) {
            throw new IllegalStateException(DUPLICATE_SAVE_MESSAGE);
        }
        encryptPassword(database);
        workingDatabaseRepository.update(database);
    }

    public void delete(WorkingDatabase database) {
        workingDatabaseRepository.deleteByCode(database.getCode());
    }

    public void createDatabase(WorkingDatabase database) {
        throw legacyNotImplemented("CreateDataBase");
    }

    public void convertToAutoDatabase(WorkingDatabase database) {
        throw legacyNotImplemented("ConvertToAutoDataBase");
    }

    public void carryForward(WorkingDatabase source, WorkingDatabase destination, boolean bulkMaterialDatabase) {
        throw legacyNotImplemented("CarryForward");
    }

    private static String nextCode(String maxCode) {
        if (maxCode == null || maxCode.isBlank()) {
            return "01";
        }
        int next = Integer.parseInt(maxCode) + 1;
        return String.format("%02d", next);
    }

    private static void encryptPassword(WorkingDatabase database) {
        database.applyEncryptedPassword(LegacyPasswordCipher.encrypt(database.getPassword()));
    }

    private static UnsupportedOperationException legacyNotImplemented(String legacyOperation) {
        return new UnsupportedOperationException(
                "Legacy WorkDataBaseFactory." + legacyOperation
                        + " maps to FoolFrame NotImplementedException");
    }
}
