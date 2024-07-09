package ai.datatower.analytics.utils

import ai.datatower.analytics.data.DataParams
import androidx.annotation.StringDef

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    DataParams.CONFIG_STATIC_SUPER_PROPERTY,
    DataParams.CONFIG_INTERNAL_SUPER_PROPERTY
)
internal annotation class CommonPropertiesKey