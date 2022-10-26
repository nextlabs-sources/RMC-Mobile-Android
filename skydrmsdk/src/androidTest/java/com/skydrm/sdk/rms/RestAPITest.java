package com.skydrm.sdk.rms;

import android.util.Log;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.Factory;
import com.skydrm.sdk.nxl.NxlFingerPrint;
import com.skydrm.sdk.rms.types.FetchLogRequestParas;
import com.skydrm.sdk.rms.types.FetchLogResult;
import com.skydrm.sdk.rms.types.HeartbeatResponse;
import com.skydrm.sdk.rms.types.RmsAddRepoResult;
import com.skydrm.sdk.rms.types.RmsUserLinkedRepos;
import com.skydrm.sdk.rms.types.SendLogRequestValue;
import com.skydrm.sdk.rms.rest.sharewithme.SharedWithMeListFileRequestParams;
import com.skydrm.sdk.rms.types.favorite.OneRepoFavFiles;
import com.skydrm.sdk.rms.types.favorite.ReposFavorite;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileParams;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.ui.uploadprogress.ProgressRequestListener;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class RestAPITest {
    // DH- prime modulus
    final static String P = "D310125B294DBD856814DFD4BAB4DC767DF6A999C9EDFA8F8D7B12551F8D71EF6032357405C7F11EE147DB0332716FC8FD85ED027585268360D16BD761563D7D1659D4D73DAED617F3E4223F48BCEFA421860C3FC4393D27545677B22459E852F5254D3AC58C0D63DD79DE2D8D868CD940DECF5A274605DB0EEE762020C39D0F6486606580EAACCE16FB70FB7C759EA9AABAB4DCBF941891B0CE94EC4D3D5954217C6E84A9274F1AB86073BDF9DC851E563B90455B8397DAE3A1B998607BB7699CEA0805A7FF013EF44FDE7AF830F1FD051FFAEC539CE4452D8229098AE3EE2008AB9DB7B2C948312CBC0137C082D6672618E1BFE5D5006E810DC7AA7F1E6EE3";
    // DH- base generator
    final static String G = "64ACEBA5F7BC803EF29731C9C6AE009B86FC5201F81BC2B8F84890FCF71CAD51C1429FD261A2A715C8946154E0E4E28EF6B2D493CC1739F5659E9F14DD14037F5FE72B3BA4D9BCB3B95B8417BDA48F118E61C8214CF8D558DA6774F08B58D97B2CCE20F5AA2F8E9539C014E7761E4E6336CFFC35127DDD527206766AE72045C11B0FF4DA76172523713B31C9F18ABABA92612BDE105141F04DB5DA3C39CDE5C6877B7F8CD96949FCC876E2C1224FB9188D714FDD6CB80682F8967833AD4B51354A8D58598E6B2DEF4571A597AD39BD3177D54B24CA518EDA996EEDBA8A31D5876EFED8AA44023CC9F13D86DCB4DDFCF389C7A1435082EF69703603638325954E";
    final static BigInteger p = new BigInteger(P, 16);
    final static BigInteger g = new BigInteger(G, 16);
    final static String TENANT = "skydrm.com";
    private static RestAPI api;
    private static Config config;
    private static IRmUser rmUser;
    private static KeyPair dhClientKeyPair;
    private static List<X509Certificate> certs;
    private static Map<String, String> mapDuidToToken;

    @BeforeClass
    public static void setUp() throws Exception {
        // use debugging  server
        config = new Config(Factory.RM_SERVER_DEBUG);
        Factory.DEVICE_ID = "awifi02:00:00:00:00:00";
        Factory.DEVICE_TYPE = "806";
        Factory.DEVICE_NAME = "Android SDK built for x86";
        Factory.CLIENT_ID = "1D5E69FD9168DF03D8779188BE160CA7";
        // ignore SSL handshake for debugging server
        api = new RestAPI(config);
    }

    @AfterClass
    public static void tearDown() throws Exception {

    }

    static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    @Test
    public void test_dhGenerateClientKey() throws Exception {
        final String algoName = "DH";
        final String formatX509 = "X.509";
        final String formatPKCS8 = "PKCS#8";
        dhClientKeyPair = api.getMembershipService(rmUser).generateDHKeyPair(p, g);
        // A : client public key
        String A_algo = dhClientKeyPair.getPublic().getAlgorithm();
        String A_format = dhClientKeyPair.getPublic().getFormat();

        Assert.assertEquals(algoName, A_algo);
        Assert.assertEquals(formatX509, A_format);

        // a : client private key
        String a_algo = dhClientKeyPair.getPrivate().getAlgorithm();
        String a_format = dhClientKeyPair.getPrivate().getFormat();

        Assert.assertEquals(algoName, a_algo);
        Assert.assertEquals(formatPKCS8, a_format);
    }

    @Test
    public void test_getLoginURLbyTenent() throws Exception {
        final String expectLoginURL = "https://rmtest.nextlabs.solutions/rms";
        String loginurl = api.getLoginService().getLoginURLbyTenant(TENANT);

        Assert.assertEquals(expectLoginURL, loginurl);

        //update config
        config.setRmsURL(loginurl);

    }


    @Test
    public void test_login() throws Exception {

//                final int userId = 36;
//        final String name = "nextlabs@126.com";
//        final String mail = "nextlabs@126.com";
//        final String pass = "123blue!";


//        final int userId = 25;
//        final String name = "osmond.ye@nextlabs.com";
//        final String mail = "osmond.ye@nextlabs.com";
//        final String mail = "lillian.wu@cn.nextlabs.com";
//        final String pass = "123blue!";

//        final String mail = "dora.yang@nextlabs.com";
//        final String pass = "123blue!";


        final String mail = "john.tyler";
        final String pass = "john.tyler";

        rmUser = api.getLoginService().basicLogin(mail, pass);
//
//        Assert.assertEquals(rmUser.getUserId(), userId);
//        Assert.assertEquals(rmUser.getName(), name);
        //  Assert.assertEquals(rmUser.getEmail(), mail);
    }

    @Test
    public void test_membership() throws Exception {
        test_login();
        test_dhGenerateClientKey();
        certs = api.getMembershipService(rmUser).membership(dhClientKeyPair.getPublic().getEncoded());
        Assert.assertEquals(3, certs.size());
        for (X509Certificate cert : certs) {
            String format = cert.getPublicKey().getFormat();
            Assert.assertEquals("X.509", format);
        }
    }

    @Test
    public void test_generateEncrytionToken() throws Exception {
        test_membership();
        // determine which certs to use
        byte[] agreementKey = api.getMembershipService(rmUser).calcDHAgreementKey(dhClientKeyPair.getPrivate().getEncoded(), certs.get(2).getPublicKey().getEncoded());

        int tokenSize = new Random().nextInt(100) + 1;
        mapDuidToToken = api.getTokenService(rmUser).getEncryptionToken(agreementKey, tokenSize);

        Assert.assertEquals(tokenSize, mapDuidToToken.size());

        for (Map.Entry<String, String> entry : mapDuidToToken.entrySet()) {
            Assert.assertNotNull(entry.getKey());
            Assert.assertNotNull(entry.getValue());
        }
    }

    @Test
    public void test_getDecryptionToken() throws Exception {
        test_generateEncrytionToken();

        // pick a pair<duid,token> randomly
        int i = new Random().nextInt(mapDuidToToken.size());
        String[] DUIDs = mapDuidToToken.keySet().toArray(new String[0]);
        String duid = DUIDs[i];
        String token = mapDuidToToken.get(duid);

        byte[] agreementKey = api.getMembershipService(rmUser).calcDHAgreementKey(dhClientKeyPair.getPrivate().getEncoded(), certs.get(2).getPublicKey().getEncoded());
        // mock data
        NxlFingerPrint fingerPrint = new NxlFingerPrint();
        fingerPrint.ml = 0;
        fingerPrint.duid = duid;
        fingerPrint.ownerId = rmUser.getMembershipId();
        fingerPrint.rootAgreementKey = bytesToHexString(agreementKey);

        // test
        String decryptToken = api.getTokenService(rmUser).getDecryptionToken(TENANT, fingerPrint);

        Assert.assertEquals(token, decryptToken);
    }

    @Test
    public void test_NxlOps() throws Exception {
        test_generateEncrytionToken();


    }

    @Test
    public void test_shareing() throws Exception {
        test_generateEncrytionToken();
        // pick a pair<duid,token> randomly
        int i = new Random().nextInt(mapDuidToToken.size());
        String[] DUIDs = mapDuidToToken.keySet().toArray(new String[0]);
        String duid = DUIDs[i];  // duid -- B17CAABE8EF50EDF781E6B34372CC5F0
        String token = mapDuidToToken.get(duid);
        String deviceID = "1012313431";
        String deviceType = "client_001";
        int rights = 275;
        List<String> listMails = new ArrayList<>(Arrays.asList("aaa111@126.com", "bbb222@126.com", "ccc333@126.com"));
        boolean result = api.getSharingService(rmUser).share(duid, token, "abc.json.com.skydrm.rmc.nxl", deviceID, deviceType, rights, listMails, System.currentTimeMillis());

        Assert.assertTrue(result);
    }


    @Test
    public void test_updateRecipients() throws Exception {
        test_login();
        String duid = "F93D0432350F3B5A67AADBFCBF4DC9E3";
        List<String> newRecipients = new ArrayList<>();
        newRecipients.add("a111d@qq.com");
        newRecipients.add("r222@qq.com");
        List<String> removeRecipients = new ArrayList<>();
        removeRecipients.add("ajld@qq.com");
        api.getSharingService(rmUser).updateRecipients(duid, newRecipients, removeRecipients, "");
    }

    @Test
    public void test_revokingDocument() throws Exception {
        // get the duid
        test_generateEncrytionToken();
        // pick a pair<duid,token> randomly
        int i = new Random().nextInt(mapDuidToToken.size());
        String[] DUIDs = mapDuidToToken.keySet().toArray(new String[0]);
        String duid = DUIDs[i];
        String deviceID = "1012313431";
        String deviceType = "client_001";
        boolean result = api.getSharingService(rmUser).revokingDocument("B17CAABE8EF50EDF781E6B34372CC5F0"); // must nxl file sharing firstly, or else 404, file not found
        Assert.assertTrue(result);
    }

    @Test
    public void test_new_sharing() throws Exception {
        test_generateEncrytionToken();

        String deviceID = "1012313431";
        String deviceType = "IPHONE"; // must be "IPHONE" ,  need ask "a3ge"
        int rights = 275;
        String nxlFile = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/nextlabs@126.com/GoogleDrive_allen.ning.520@gmail.com/ROOT/0ByhXxmQ6nb1CQkxIbHR6VVE5OXM/73a1cd0a9bb342779af6546cd434e4a3-2016-12-26-15-42-11.jpg.nxl";
        List<String> listMails = new ArrayList<>(Arrays.asList("aaa111@126.com", "bbb222@126.com", "ccc333@126.com"));
        api.getSharingService(rmUser).share(new File(nxlFile), "ITAR, US", false, deviceID, deviceType, rights, listMails, System.currentTimeMillis());
    }

    @Test
    public void test_myHeartbeatV2() throws Exception {
        test_login();
        HeartbeatResponse result = api.getHeartbeatService(rmUser).heartbeat();
        Assert.assertNotNull(result);
    }

    @Test
    public void test_myDriveList() throws Exception {
        test_login();
        String path = "/";
        JSONObject result = api.getMyDriveService(rmUser).myDriveList(path);

        Assert.assertNotNull(result);


        result = api.getMyDriveService(rmUser).myDriveList("/ccc");
        result = api.getMyDriveService(rmUser).myDriveList("/ccc/");
        result = api.getMyDriveService(rmUser).myDriveList("/ccc/dfds.txt");


    }

    @Test
    public void test_myDriveStorageUsed() throws Exception {
        test_login();
        JSONObject result = api.getMyDriveService(rmUser).myDriveStorageUsed();

        Assert.assertNotNull(result);

    }

    @Test
    public void test_myDriveCreatePublicShare() throws Exception {
        test_login();
        //String path="/ccc/dfds.txt";
        //String path="/ccc/sdfdsfsdfsdf.txt";
        String path = "/ccc/RM Everest Team Member E-mail Addresses.docx";
        boolean recursive = true;
        String result = api.getMyDriveService(rmUser).myDriveCreatePublicShare(path);

        Assert.assertNotNull(result);

    }

    @Test
    public void test_myDriveUpload() throws Exception {
        test_login();
//        String path = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/osmond.ye@nextlabs.com/DropBox_419996767/ROOT/88888-2016-12-07-09-59-42.txt.nxl";
        String path = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/pdf.pdf";
        String cloud = "/oye/pdf.pdf";
        File f = new File(path);

//        String result = api.myDriveUpload(rmUser, cloud, f);

//        Assert.assertNotNull(result);
    }

    @Test
    public void test_myDriveCreateFolder() throws Exception {
        test_login();
        String path = "/abc/ddd";

//        boolean result = api.myDriveCreateFolder(rmUser, path);

//        Assert.assertTrue(result);

    }

    @Test
    public void test_myDriveDownload() throws Exception {
        test_login();
        String localPath = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/abc.txt";
        String path = "/RM Everest Team Member E-mail Addresses.docx";
        String bigPath = "/BigFile/Boost.Asio C++ Network Programming.pdf";
        long fileSize = 1000000;
        api.getMyDriveService(rmUser).myDriveDownload(bigPath, fileSize, localPath, new RestAPI.DownloadListener() {
            @Override
            public void current(int i) {
                System.out.println(i);
            }

            @Override
            public void cancel() {

            }
        });
    }

    @Test
    public void test_updateProfile() throws Exception {
        test_login();
        String path = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/header.jpg";
        File f = new File(path);
        Assert.assertTrue(f.exists());
        FileInputStream inputStream = new FileInputStream(f);
        byte[] buffer = new byte[(int) f.length()];
        inputStream.read(buffer);
        inputStream.close();
        api.getUserService(rmUser).updateUserProfile(buffer, null);
//        Assert.assertNotNull(s);
        // assert result from api.updateUserProfile
    }

    @Test
    public void test_retrieveProfile() throws Exception {
        test_login();
//        String s = api.retrieveUserProfile(rmUser);
//        Assert.assertNotNull(s);
    }

    @Test
    public void test_repository() throws Exception {
        test_login();
        String s;
        //test add
        //- generate a default one
        RmsUserLinkedRepos.ResultsBean.RepoItemsBean testRepo = RmsUserLinkedRepos.ResultsBean.RepoItemsBean.buildDefault();
        //- modify some fields
        testRepo.setName("userDefinedNickName");
        /*
         DROPBOX
         GOOGLE_DRIVE
         ONE_DRIVE
         SHAREPOINT_ONPREMISE
         SHAREPOINT_ONLINE
         SHAREPOINT_CROSSLAUNCH
         SHAREPOINT_ONLINE_CROSSLAUNCH
         */
        testRepo.setType("DROPBOX");
        testRepo.setAccountId("testAccountId");
        testRepo.setAccountName("testAccountName");
        testRepo.setPreference("");
        testRepo.setToken("DrobBoxAccessToken");

        RmsAddRepoResult repoResult = api.getRepositoryService(rmUser).repositoryAdd(testRepo, null);

        Assert.assertEquals(200, repoResult.getStatusCode());
        Assert.assertNotNull(repoResult.getResults().getRepoId());
        //  we use api.repositoryAdd to get the new generated repo's id
        testRepo.setRepoId(repoResult.getResults().getRepoId());


        // test update
        String nickName = "aNewNickName";
        String testToken = "aNewTestToken";
        boolean updateStatus = api.getRepositoryService(rmUser).repositoryUpdate(
                repoResult.getResults().getRepoId(),
                nickName,
                testToken);
        Assert.assertEquals(updateStatus, true);

        //----------------------------
        //test get
        RmsUserLinkedRepos.ResultsBean resultsBean = api.getRepositoryService(rmUser).repositoryGet();
        Assert.assertNotNull(resultsBean);
        // test with new nicked
        //----------------------------
        //delete
        s = api.getRepositoryService(rmUser).repositoryRemove(repoResult.getResults().getRepoId());
        Assert.assertNotNull(s);
    }

    @Test
    public void test_sendLog() throws Exception {
        test_login();
        String filePath = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/picture.png";
        String fileName = "picture.png";
        test_generateEncrytionToken();
        // pick a pair<duid,token> randomly
        int i = new Random().nextInt(mapDuidToToken.size());
        String[] DUIDs = mapDuidToToken.keySet().toArray(new String[0]);
        String duid = DUIDs[i];
        String token = mapDuidToToken.get(duid);

        SendLogRequestValue logRequestValue = new SendLogRequestValue();
        logRequestValue.setmDuid(duid);
        logRequestValue.setmOwnerId(rmUser.getMembershipId());
        logRequestValue.setmUserId(rmUser.getUserId());
        logRequestValue.setmOperationId(SendLogRequestValue.OperationType.VIEW);
        logRequestValue.setmDeviceId("1012313431");
        logRequestValue.setmFilePathId("client_001");
        logRequestValue.setmRepositoryId("");
        logRequestValue.setmFilePathId("");
        logRequestValue.setmFileName(fileName);
        logRequestValue.setmFilePath(filePath);
        logRequestValue.setmAppName("RMC Android");
        logRequestValue.setmAppPath("RMC Android");
        logRequestValue.setmAppPublisher("Nextlabs");
        logRequestValue.setmAccessResult(SendLogRequestValue.AccessResult.Allowed);
        logRequestValue.setmAccessTime(System.currentTimeMillis());
        logRequestValue.setmActivityData("");

        boolean result = api.getLogService(rmUser).sendLogToRms(logRequestValue);
        Assert.assertTrue(result);
    }

    @Test
    public void test_upLoadProgress() throws Exception {
        try {
            test_login();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String filePath = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/tmp/copyFromMailAttachment/123-2017-01-06-08-48-48.png.nxl";
        try {
            MyVaultUploadFileParams params = new MyVaultUploadFileParams.Builder()
                    .setNxlFile(new File(filePath))
                    .setSrcPathId(filePath)
                    .setSrcPathDisplay(filePath)
                    .setSrcRepoName("myDropBox")
                    .setSrcRepoType("DropBox")
                    .build();
            api.getMyVaultService(rmUser).uploadFileToMyVault(params, new ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) throws IOException {
                    Log.d("test_upLoadProgress", "bytesWritten: =" + bytesWritten + "contentLength =" + contentLength + "done =" + done);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_myVaultDownloadFile() throws Exception {
        test_login();
//        /nxl_myvault_nxl/strings-2016-12-29-08-05-58.txt.nxl    --- //   /nxl_myvault_nxl/strings-2016-12-29-08-05-58.txt.nxl
//        MyVaultDownloadHeader myVaultDownloadHeader = api.myVaultDownloadFile(rmUser, "/rrr-2016-12-30-03-02-42.txt.nxl", "rrr-2016-12-30-03-02-42.txt.nxl", new RestAPI.DownloadListener() {
//            @Override
//            public void current(int i) {
//                Log.d("Download", "current: " + i);
//            }
//
//            @Override
//            public void cancel() {
//
//            }
//        });
    }

    @Test
    public void test_myVaultDeleteFile() throws Exception {
        test_login();

        String filePathid = "/deletefile.txt";
        //  boolean myVaultDeleteFile = api.myVaultDeleteFile(rmUser, filePathid);

    }

    @Test
    public void test_myVaultMetaData() throws Exception {

    }

    @Test
    public void test_getCaptcha() throws Exception {
        test_login();
        //byte[] captcha = api.getCaptcha();
        // Assert.assertNotNull(captcha);
    }

    @Test
    public void text_sendCaptcha() throws Exception {
        test_login();
        api.getUserService(rmUser).sendCaptcha("client_0001%40nextlabs.com", "B303E6DFE546A889498956A973895A3E57688BC7", "12345");
    }

    @Test
    public void test_myDriveUploadProgress() throws Exception {
        test_login();
//        String path = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/nextlabs@126.com/myDrive_nextlabs@126.com/ROOT/tcp.js";
//        String path = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/header.jpg";
        String path = "/storage/emulated/0/header.jpg";

        String cloud = "/oye/header.jpg";
        File f = new File(path);

//        String result = api.myDriveUploadProgress(rmUser, cloud, f, new ProgressRequestListener() {
//            @Override
//            public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
//                Log.d("myDriveUploadProgress", "bytesWritten: =" + bytesWritten + "  " + "contentLength: =" + contentLength + "  " + "done: =" + done);
//            }
//        });
//
//        Assert.assertNotNull(result);
    }

    @Test
    public void test_fetchActivityLog() throws Exception {
        test_generateEncrytionToken();
        // pick a pair<duid,token> randomly
        int i = new Random().nextInt(mapDuidToToken.size());
        String[] DUIDs = mapDuidToToken.keySet().toArray(new String[0]);
        String duid = DUIDs[i];  // duid -- B17CAABE8EF50EDF781E6B34372CC5F0

        FetchLogResult fetchLogResult = api.getLogService(rmUser).fetchActivityLog(duid, new FetchLogRequestParas());
    }


//    @Test
//    public void test_listProjects() throws Exception {
//        test_login();
//        api.listProjects(rmUser, true);
//    }

    @Test
    public void test_getProjectMetaData() throws Exception {
        test_login();
        int projectId = 168;
        api.getProjectMetaData(rmUser, projectId);
    }

    @Test
    public void test_projectUploadFile() throws Exception {
        test_login();
        String filePath = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/nextlabs@126.com/GoogleDrive_nextlabs.test.01@gmail.com/ROOT/0B9fkqMCinzE7QnFITXN3Uk5Xb3c/for_test.xlsx";
        //File file = new File("/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/nextlabs@126.com/GoogleDrive_allen.ning.520@gmail.com/ROOT/0ByhXxmQ6nb1CVXRtRXZEZldMaUk/73a1cd0a9bb342779af6546cd434e4a3.jpg");
        File file = new File(filePath);
        List<String> list = new ArrayList<>();
        list.add("VIEW");
        list.add("SHARE");
        String tags = "Confidentiality=SECRET,TOP SECRET";
//        api.projectUploadFile(rmUser, 168, file, list, "/test/", "/test", tags, new ProgressRequestListener() {
//            @Override
//            public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
//
//            }
//        });
    }

    @Test
    public void test_projectCreateFolder() throws Exception {
        test_login();
//        api.projectCreateFolder(rmUser, 168, "/test/subFolder", false);
    }

    @Test
    public void test_projectDelete() throws Exception {
        test_login();
//        api.projectDeleteFileFolder(rmUser, 168, "/newCreateFolder/");
    }
    @Test
    public void test_projectDownload() throws Exception {
        test_login();
        String filePath = "/test/for_test-2017-02-09-07-44-48.xlsx.nxl";
        String localPath = "/storage/emulated/0/Android/data/com.skydrm.rmc/files/cache/lillian.wu@cn.nextlabs.com/SharePoint Online_SharePoint Online_https%3A%2F%2Fnextlabsdev.sharepoint.com%2FProjectNova%0Amxu%40nextlabsdev.onmicrosoft.com/ROOT/EEEEEEEE/for_test-2017-02-09-07-44-48.xlsx.nxl";
        api.projectDownloadFile(rmUser, 168, filePath, localPath, 1, new RestAPI.DownloadListener() {
            @Override
            public void current(int i) {

            }

            @Override
            public void cancel() {

            }
        });
    }

    @Test
    public void projectGetMembership() throws Exception {
        test_login();
        api.getProjectService(rmUser).getProjectMembershipId(21);
    }
    //--------------------------end test project

    @Test
    public void test_SharedWithMe() throws Exception {
        test_login();
        String id = "8f5c5d3f-ccdb-4d5b-96fa-60d97a880f0b";
        String code = "086894A8BB87428E2F2320A8D068FCC1552452EDD080CEE74A311BBC9FAEC2F7";
        File f = new File("/storage/emulated/0/Android/data/com.skydrm.rmc/files/adb.txt");
        f.getParentFile().mkdirs();
        if (!f.createNewFile()) {
            f.delete();
            f.createNewFile();
        }


        api.getSharedWithMeService(rmUser).download(id, code, false, f, 0, 0, new RestAPI.DownloadListener() {
            @Override
            public void current(int i) {

            }

            @Override
            public void cancel() {

            }
        });
    }

    @Test
    public void test_ShareWithMeListFile() throws Exception {
        test_login();
        api.getSharedWithMeService(rmUser).listFile(new SharedWithMeListFileRequestParams());
    }

    @Test
    public void test_getFavoriteFilesInAllRepos() throws Exception {
        test_login();
        ReposFavorite reposFavorite = api.getFavoriteService(rmUser).getFavoriteFilesInAllRepos();
    }

    @Test
    public void test_getFavoriteFileListInAllRepos() throws Exception {
        test_login();
//        AllRepoFavFileList allRepoFavFileList = api.getFavoriteService(rmUser).getFavoriteFileListInAllRepos(new AllRepoFavFileRequestParas());
    }

    @Test
    public void test_getFavoriteFilesInOneRepo() throws Exception {
        test_login();
        String repoId = "c2247e28-815d-471c-aff3-64c244be6d0e";
        OneRepoFavFiles oneRepoFavFiles = api.getFavoriteService(rmUser).getFavoriteFilesInOneRepo(repoId, null);
    }
}