-keep class com.roiquery.analytics.ROIQueryAnalytics{*;}
-keep class com.roiquery.analytics.ROIQueryAnalytics$Companion{
    public <methods>;
}
-keep class com.roiquery.analytics.DTAnalytics{*;}
-keep class com.roiquery.analytics.DTAnalytics$Companion{
    public <methods>;
}


-keep class com.android.installreferrer.*{*;}
-keep class com.google.android.*{*;}

-keep class com.roiquery.analytics.ROIQuery{*;}
-keep class com.roiquery.analytics.ROIQuery$Companion{
    public <methods>;
}
-keep class com.roiquery.analytics.DT{*;}
-keep class com.roiquery.analytics.DT$Companion{
    public <methods>;
}

-keep class com.roiquery.analytics.DTAnalyticsUtils{*;}
-keep class com.roiquery.analytics.DTAnalyticsUtils$Companion{
    public <methods>;
}


-keep class com.roiquery.analytics.ROIQueryChannel{*;}
-keep class com.roiquery.analytics.DTChannel{*;}
-keep class com.roiquery.analytics.InitCallback{*;}

-keep enum com.roiquery.ad.*{*;}
-keep class com.roiquery.ad.ROIQueryAdReport{*;}
-keep class com.roiquery.ad.ROIQueryAdReport$Companion{*;}

-keep class com.roiquery.ad.DTAdReport{*;}
-keep class com.roiquery.ad.DTAdReport$Companion{*;}

-keep class com.roiquery.iap.ROIQueryIAPReport{*;}
-keep class com.roiquery.iap.ROIQueryIAPReport$Companion{*;}
-keep class com.roiquery.iap.DTIAPReport{*;}
-keep class com.roiquery.iap.DTIAPReport$Companion{*;}

-keep class com.roiquery.ias.ROIQueryIasReport{*;}
-keep class com.roiquery.ias.ROIQueryIasReport$Companion{*;}

-keep class com.roiquery.ias.DTIASReport{*;}
-keep class com.roiquery.ias.DTIASReport$Companion{*;}

-keep class com.roiquery.thirdparty.ThirdSDKShareType{*;}

-keeppackagenames com.roiquery.*