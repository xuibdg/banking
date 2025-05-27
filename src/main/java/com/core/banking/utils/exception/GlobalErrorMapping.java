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
    CLOSED_ACCOUNT_FAILED("IEG-0023", "Cannot close account with non-zero balance."),
    DORMANT_ACCOUNT_FAILED("IEG-0024", "Cannot mark a CLOSED account as DORMANT."),
    BLOCK_ACCOUNT_FAILED("IEG-0025", "Cannot BLOCK a CLOSED account."),
    CUSTOMER_NOT_FOUND("IEG-0026", "Customer does not exist."),
    DATA_NOT_FOUND("IEG-0027", "Data not found or does not exist."),
    NEGATIVE_INITIAL_DEPOSIT("IEG-0028", "Initial deposit cannot be negative."),
    MINIMUM_INITIAL_DEPOSIT("IEG-0029", "Initial deposit less than minimum."),
    MAXIMUM_INITIAL_DEPOSIT("IEG-0030", "Initial deposit more than maximum."),
    ;


    public final String code;
    public final String message;

    GlobalErrorMapping(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
