package ai.datatower.analytics.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

 class NotNullSingleVar<T>:ReadWriteProperty<Any?,T>{
    private var mValue:T? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
       return mValue ?: throw IllegalStateException()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.mValue == null && value != null) this.mValue = value
        else LogUtils.v("illegal value ,can not set a exist value")
    }

}