package ai.datatower.thirdparty

class ThirdPartManager private constructor(){
    companion object{
        val  instance by lazy {
            ThirdPartManager()
        }
    }
}