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
    ID_NOT_FOUND("IEG-0300", "ID Tidak ditemukan. pastikan id yang dimasukkan sesuai!."),
    AMOUNT_NOT_ENOUGH("IEG-0301", "Amount tidak sesuai dengan yang telah ditentukan!."),
    CUSTOMER_NOT_ACTIVE("IEG-0302", "Customer tersebut tidak active!."),
    NOMINAL_NOT_ENOUGHT("IEG-0303", "Nominal tidak sesuai dengan yang telah ditentukan!."),
    DURATION_NOT_ENOUGHT("IEG-0304", "Durasi tidak sesuai dengan yang telah ditentukan!."),
    NOT_PENDING_APPROVAL("IEG-0305", "Status bukan PENDING_APPROVAL!."),
    NOT_PENDING("IEG-0306", "Status pembayaran bukan PENDING!."),
    NOT_ACTIVE("IEG-0307", "Status akun bukan tidak ACTIVE!."),
    CUSTOMER_BORROW("IEG-0308", "Customer sudah memiliki pinjaman aktif!."),

    ;


    public final String code;
    public final String message;

    GlobalErrorMapping(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
