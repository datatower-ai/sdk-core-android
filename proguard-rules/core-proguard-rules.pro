-keep class com.roiquery.analytics.ROIQueryAnalytics{*;}
-keep class com.roiquery.analytics.ROIQueryAnalytics$Companion{
    public <methods>;
}
-keep class com.android.installreferrer.*{*;}
-keep class com.google.android.*{*;}

-keep class com.roiquery.analytics.ROIQuery{*;}
-keep class com.roiquery.analytics.ROIQuery$Companion{
    public <methods>;
}

-keep class com.roiquery.cloudconfig.ROIQueryCloudConfig{*;}
-keep class com.roiquery.cloudconfig.ROIQueryCloudConfig$Companion{
    public <methods>;
}

-keep class com.roiquery.analytics.utils.**{*;}
-keep class com.roiquery.cloudconfig.utils.**{*;}
-keep class com.roiquery.analytics.ROIQueryChannel{*;}

-keep enum com.roiquery.ad.*{*;}
-keep class com.roiquery.ad.ROIQueryAdReport{*;}
-keep class com.roiquery.ad.ROIQueryAdReport$Companion{*;}

-keep class com.roiquery.ad.utils.UUIDUtils{*;}
-keep class com.roiquery.iap.ROIQueryIAPReport{*;}
-keep class com.roiquery.iap.ROIQueryIAPReport$Companion{*;}

-keeppackagenames com.roiquery.iap
-keeppackagenames com.roiquery.ad
-keeppackagenames com.roiquery.analytics
-keeppackagenames com.roiquery.cloudconfig