package com.app.faizanzw.network

import javax.inject.Qualifier

public class Qualifiers {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class baseClientWithToken

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class nodeClientWithToken
}