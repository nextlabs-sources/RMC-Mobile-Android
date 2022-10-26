package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import okhttp3.OkHttpClient;

interface IHttpClient {
    OkHttpClient createClient();
}
