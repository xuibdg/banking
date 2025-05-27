package com.core.banking.utils.exception;

public enum GlobalErrorMapping {
    SYSTEM_ERROR("-1", "Error silahkan kontak tim kami"),
    SUCCESS("0", "SUCCESS"),
    ERROR("1", "ERROR"),
    PRODUCT_NOT_FOUND("IEG-0014", "Product tidak ditemukan atau product tidak aktif."),
    DATA_NOT_FOUND_CUSTOM("IEG-0012", "Data ${1} tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    DATA_NOT_FOUND("IEG-0012", "Data tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    RULE_NOT_FOUND("IEG-0013", "Data RULE tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    STOCK_NOT_FOUND("IEG-0015", "Stock tidak ditemukan."),
    CASHIER_NOT_FOUND("IEG-0016", "Data Cashier ID tidak ditemukan. Pastikan cashier id yang anda masukan sesuai dengan kondisi di database."),
    PRODUCT_STOCK_NOT_ENOUGH("IEG-0017", "Stock product yang dipilih, saat ini stock product tersebut kurang dari quantity. Data Product Stock tidak dapat memenuhi permintaan pelanggan."),
    PRODUCT_ID_NOT_FOUND("IEG-0018", "Data Product ID tidak ditemukan. Pastikan product id yang anda masukan sesuai dengan kondisi di database"),
    NOMINAL_NOT_ENOUGH("IEG-0019", "Uang pelanggan kurang. Pelanggan tidak dapat membayar harga satuan produk, dikali quantity."),
    DATA_USER_NOT_FOUND("IEG-0020", "Data USER tidak ditemukan. Pastikan Value yang anda masukan sudah sesuai"),
    INVALID_CREDENTIAL("IEG-0021", "Username atau password salah."),
    UNAUTHORIZED_ACCESS("IEG-0022", "Akses tidak diizinkan. Anda tidak memiliki hak untuk mengakses resource ini."),
    DATA_ALREADY_EXIST("IEG-0023", "Data ditemukan. Pastikan data yang anda masukan belum terdaftar"),
    INSUFFICIENT_AGE("IEG-0024", "Umur harus lebih dari 17 tahun"),
    CUSTOMER_INACTIVE("IEG-0025", "Tidak bisa memperbaharui data customer, silakan melakukan update status terlebih dahulu"),
    ACCOUNT_CLOSE_RESTRICTED("IEG-0025", "Rekening tidak dapat ditutup karena masih terdapat data tabungan atau pinjaman yang aktif"),
    EMAIL_ALREADY_EXIST("IEG-0026", "Email ditemukan. Pastikan email yang anda masukan belum terdaftar"),
    PHONE_ALREADY_EXIST("IEG-0027", "Nomor Telepon ditemukan. Pastikan nomor yang anda masukan belum terdaftar"),
    NOT_FOUND_ID("IEG-0028", "Data ID Tidak Ditemukan"),
    CLOSED_ACCOUNT_FAILED("IEG-0029", "Cannot close account with non-zero balance."),
    DORMANT_ACCOUNT_FAILED("IEG-0030", "Cannot mark a CLOSED account as DORMANT."),
    BLOCK_ACCOUNT_FAILED("IEG-0031", "Cannot BLOCK a CLOSED account."),
    CUSTOMER_NOT_FOUND("IEG-0032", "Customer does not exist."),
    NEGATIVE_INITIAL_DEPOSIT("IEG-0033", "Initial deposit cannot be negative."),
    MINIMUM_INITIAL_DEPOSIT("IEG-0034", "Initial deposit less than minimum."),
    MAXIMUM_INITIAL_DEPOSIT("IEG-0035", "Initial deposit more than maximum."),
    ;





    public final String code;
    public final String message;

    GlobalErrorMapping(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
