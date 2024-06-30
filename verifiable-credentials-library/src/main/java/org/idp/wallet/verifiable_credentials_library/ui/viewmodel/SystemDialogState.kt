package org.idp.wallet.verifiable_credentials_library.ui.viewmodel

data class SystemDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val onClickPositiveButton: () -> Unit = {},
    val onClickNegativeButton: () -> Unit = {}
) {}
