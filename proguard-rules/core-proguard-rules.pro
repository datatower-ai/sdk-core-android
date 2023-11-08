-keep class ai.datatower.analytics.ROIQueryAnalytics{*;}
-keep class ai.datatower.analytics.ROIQueryAnalytics$Companion{
    public <methods>;
}
-keep class ai.datatower.analytics.DTAnalytics{*;}
-keep class ai.datatower.analytics.DTAnalytics$Companion{
    public <methods>;
}


-keep class com.android.installreferrer.*{*;}
-keep class com.google.android.*{*;}

-keep class ai.datatower.analytics.ROIQuery{*;}
-keep class ai.datatower.analytics.ROIQuery$Companion{
    public <methods>;
}
-keep class ai.datatower.analytics.DT{*;}
-keep class ai.datatower.analytics.DT$Companion{
    public <methods>;
}

-keep class ai.datatower.analytics.DTAnalyticsUtils{*;}
-keep class ai.datatower.analytics.DTAnalyticsUtils$Companion{
    public <methods>;
}


-keep class ai.datatower.analytics.ROIQueryChannel{*;}
-keep class ai.datatower.analytics.DTChannel{*;}
-keep class ai.datatower.analytics.DTThirdPartyShareType{*;}
-keep class ai.datatower.analytics.OnDataTowerIdListener{*;}

-keep enum ai.datatower.ad.*{*;}
-keep class ai.datatower.ad.ROIQueryAdReport{*;}
-keep class ai.datatower.ad.ROIQueryAdReport$Companion{*;}

-keep class ai.datatower.ad.DTAdReport{*;}
-keep class ai.datatower.ad.DTAdReport$Companion{*;}

-keep class ai.datatower.iap.ROIQueryIAPReport{*;}
-keep class ai.datatower.iap.ROIQueryIAPReport$Companion{*;}
-keep class ai.datatower.iap.DTIAPReport{*;}
-keep class ai.datatower.iap.DTIAPReport$Companion{*;}

-keep class ai.datatower.ias.ROIQueryIasReport{*;}
-keep class ai.datatower.ias.ROIQueryIasReport$Companion{*;}

-keep class ai.datatower.ias.DTIASReport{*;}
-keep class ai.datatower.ias.DTIASReport$Companion{*;}


-keeppackagenames ai.datatower.**