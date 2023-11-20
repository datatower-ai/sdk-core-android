-keep class com.android.installreferrer.*{*;}
-keep class com.google.android.*{*;}

-keep class ai.datatower.analytics.DT{*;}
-keep class ai.datatower.analytics.DT$Companion{
    public <methods>;
}

-keep class ai.datatower.analytics.DTAnalytics{*;}
-keep class ai.datatower.analytics.DTAnalytics$Companion{
    public <methods>;
}

-keep class ai.datatower.analytics.DTAnalyticsUtils{*;}
-keep class ai.datatower.analytics.DTAnalyticsUtils$Companion{
    public <methods>;
}

-keep class ai.datatower.analytics.DTChannel{*;}
-keep class ai.datatower.analytics.DTThirdPartyShareType{*;}
-keep class ai.datatower.analytics.OnDataTowerIdListener{*;}

-keep enum ai.datatower.ad.*{*;}
-keep class ai.datatower.ad.DTAdReport{*;}
-keep class ai.datatower.ad.DTAdReport$Companion{*;}

-keep class ai.datatower.iap.DTIAPReport{*;}
-keep class ai.datatower.iap.DTIAPReport$Companion{*;}

-keep class ai.datatower.ias.DTIASReport{*;}
-keep class ai.datatower.ias.DTIASReport$Companion{*;}


-keeppackagenames ai.datatower.**