package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import okhttp3.OkHttpClient;

public interface IResource {
    String getResources(OkHttpClient client, String url) throws Exception;
}
