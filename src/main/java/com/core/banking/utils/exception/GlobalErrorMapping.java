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
    SAVING_ACCOUNT_NOT_FOUND("IEG-0101", "Data Saving Account tidak ditemukan."),
    INVALID_DEPOSIT_AMOUNT("IEG-0102", "Jumlah deposit tidak valid. Harus lebih besar dari nol."),
    INVALID_WITHDRAWAL_AMOUNT("IEG-0103", "Jumlah penarikan tidak valid. Harus lebih besar dari nol."),
    INSUFFICIENT_BALANCE("IEG-0104", "Saldo tabungan tidak cukup untuk melakukan penarikan."),
    MAX_BALANCE_EXCEEDED("IEG-0105", "limit transaksi yang telah ditetapkan telah terlampaui"),
    INVALID_DATE_RANGE("IEG-0106", "Tanggal mulai tidak boleh setelah tanggal akhir pernyataan"),
    MISSING_ACCOUNT_ID("IEG-0107", "Saving account ID Diperlukan Untuk Laporan"),
    DAILY_NOMINAL_LIMIT_EXCEEDED("IEG-0108", "melebihi batas nominal transaksi harian"),
    DAILY_COUNT_LIMIT_EXCEEDED("IEG-0109", "melebihi batas pengambilan"),
    ACCOUNT_NOT_ACTIVE("IEG-0110", "Akun tidak aktif"),
    MIN_BALANCE_VIOLATED("IEG-0111", "Kurang dari Saldo Minimal"),
    INVALID_PAGE_PARAM("IEG-0112", "Nomer Page tidak Boleh Negatif"),
    INVALID_PAGE_SIZE_PARAM("IEG-0113","halaman harus 1-100 Halaman"),
    SAVING_CONFIG_NOT_FOUND ("IEG-0114", "Saving Config tidak ditemukan"),
    TRX_REF_GENERATION_FAILED ("IEG-0115", "gagal generate code"),
    ESCROW_ACCOUNT_NOT_FUNDED ("IEG-0116", "Escrow account harus dalam status funded"),
    ESCROW_INSUFFICIENT_BALANCE ("IEG-0117","Dana di escrow akun tidak memadai")

    ;


    public final String code;
    public final String message;

    GlobalErrorMapping(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
