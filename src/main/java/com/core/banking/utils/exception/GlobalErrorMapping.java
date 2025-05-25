package com.core.banking.utils.exception;

public enum GlobalErrorMapping {
    SYSTEM_ERROR("-1", "Error silahkan kontak tim kami"),
    SUCCESS("0", "SUCCESS"),
    ERROR("1", "ERROR"),
    DATA_NOT_FOUND_CUSTOM("IEG-0012", "Data ${1} tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    RULE_NOT_FOUND("IEG-0013", "Data RULE tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    PRODUCT_NOT_FOUND("IEG-0014", "Product tidak ditemukan atau product tidak aktif."),
    STOCK_NOT_FOUND("IEG-0015", "Stock tidak ditemukan."),
    CASHIER_NOT_FOUND("IEG-0016", "Data Cashier ID tidak ditemukan. Pastikan cashier id yang anda masukan sesuai dengan kondisi di database."),
    PRODUCT_STOCK_NOT_ENOUGH("IEG-0017", "Stock product yang dipilih, saat ini stock product tersebut kurang dari quantity. Data Product Stock tidak dapat memenuhi permintaan pelanggan."),
    PRODUCT_ID_NOT_FOUND("IEG-0018", "Data Product ID tidak ditemukan. Pastikan product id yang anda masukan sesuai dengan kondisi di database"),
    NOMINAL_NOT_ENOUGH("IEG-0019", "Uang pelanggan kurang. Pelanggan tidak dapat membayar harga satuan produk, dikali quantity."),
    DATA_USER_NOT_FOUND("IEG-0020", "Data USER tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    INVALID_CREDENTIAL("IEG-0021", "Username atau password salah."),
    UNAUTHORIZED_ACCESS("IEG-0022", "Akses tidak diizinkan. Anda tidak memiliki hak untuk mengakses resource ini."),
    ACC_NUMBER_REQ("IEG-0023", "Account number diperlukan. Hint: Pastikan account number ada"),
    ACC_NUMBER_EXIST("IEG-0024", "Account number telah ada. Hint: Pastikan masukan account number yang berbeda."),
    CUSTOMER_NOT_FOUND("DEP-0001", "Data Customer tidak ditemukan"),
    CUSTOMER_NOT_ACTIVE("DEP-0002", "Customer tidak dalam status aktif"),
    DEPOSIT_TYPE_CONFIG_NOT_FOUND("DEP-0003", "Konfigurasi jenis deposito tidak ditemukan. Hint: Cek teliti deposit type config id."),
    DEPOSIT_TYPE_CONFIG_NOT_ACTIVE("DEP-0004", "Konfigurasi jenis deposito tidak aktif"),
    DEPOSIT_AMOUNT_BELOW_MINIMUM("DEP-0005", "Jumlah setoran tidak memenuhi minimum deposit"),
    DEPOSIT_ACCOUNT_NOT_FOUND("DEP-0006", "Rekening deposito tidak ditemukan");


    public final String code;
    public final String message;

    GlobalErrorMapping(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
