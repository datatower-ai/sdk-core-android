#!/bin/bash



modifyDependenceType(){
  #获取当前 maven 仓库的type
  # shellcheck disable=SC2006
  dependence_type=$(cat -v ./build.gradle | grep ext.dependence_type |awk -F "=" '{print $2}')


  # 修改maven 仓库为远程模式
  if [ $dependence_type != "\"2\"" ]; then
    sed -i.bak "s/ext.dependence_type.*=.*$dependence_type/ext.dependence_type = \"2\"/g" build.gradle
    rm -rf build.gradle.bak
    git commit build.gradle -m "update maven type"
    if [ $? -ne 0 ]; then
        exit
    fi
  fi
}


modifyVersionName(){
  aar_version_name=$1
  aar_type=$2
  versionName=("${aar_type}VersionName")
  #获取aar 版本名称
  # shellcheck disable=SC2128
  version_name=$(cat -v ./conf.gradle | grep  "${versionName}")
  old_version_name=$(echo "${version_name##*versionName}" |awk -F ":" '{print $2}' |awk -F "," '{print $1}'|awk -F "'" '{print $2}')

  #比较输入的版本名称，不同则修改
  if [ "${aar_version_name}" != "${old_version_name}" ]; then
      # shellcheck disable=SC2128
      sed -i.bak "s/${version_name}/\t\t\t${versionName}      :'${aar_version_name}',/g" conf.gradle
      rm -rf conf.gradle.bak
  fi

}


modifyVersionCode(){
  aar_version_code=$1
  aar_type=$2
  versionCode=("${aar_type}VersionCode")
  #获取aar 版本号
  # shellcheck disable=SC2128
  version_code=$(cat -v ./conf.gradle | grep  "${versionCode}")
  old_version_code=$(echo "${version_code##*versionCode}" |awk -F ":" '{print $2}' |awk -F "," '{print $1}')


  #比较输入的版本号，不同则修改
  if [ "${aar_version_code}" != "${old_version_code}" ]; then
      # shellcheck disable=SC2128
      sed -i.bak "s/${version_code}/\t\t\t${versionCode}      :${aar_version_code},/g" conf.gradle
      rm -rf conf.gradle.bak
  fi

}


mavenAarByType(){
  aar_type=$1
  echo ---------------------${aar_type}  start --------------------------

  version_name=$(cat -v ./conf.gradle | grep  "${aar_type}VersionName")
  version_code=$(cat -v ./conf.gradle | grep  "${aar_type}VersionCode")
  old_version_code=$(echo "${version_code##*versionCode}" |awk -F ":" '{print $2}' |awk -F "," '{print $1}')
  old_version_name=$(echo "${version_name##*versionName}" |awk -F ":" '{print $2}' |awk -F "," '{print $1}'|awk -F "'" '{print $2}')

  echo  "请输入${aar_type} aar 版本名称(eg: 1.0.25),当前版本为${old_version_name}:"
  read aar_version_name

  echo  "请输入${aar_type} 版本号,当前版本号为${old_version_code}:"
  read aar_version_code

  modifyVersionName "${aar_version_name}"  "${aar_type}"
  modifyVersionCode  "${aar_version_code}" "${aar_type}"

    # shellcheck disable=SC2164
  cd roiquery-"${aar_type}"
  gradle clean
  gradle publish
    if [  $? -ne 0 ]; then
        exit
    fi
  cd ..

  git commit  conf.gradle -m "update ${aar_type} versionName ${aar_version_name}, versionCode ${aar_version_code}"
  git tag "${aar_type}"/${aar_version_name}

  if [  $? -ne 0 ]; then
      exit
  fi

  echo ---------------------${aar_type}  end --------------------------
}

modifyDependenceType
git pull origin master
if [  $? -ne 0 ]; then
    exit
fi
echo "是否需要修改core 版本:（yes/no or Y/N)"
read -r need_update_core_version

if [ "${need_update_core_version}" == "yes" ]  || [ "${need_update_core_version}" == "Y" ]; then
   mavenAarByType core
fi


echo "是否需要修改ad 版本:（yes/no or Y/N）"
read -r need_update_ad_version
if [ "${need_update_ad_version}" == "yes" ]  || [ "${need_update_ad_version}" == "Y" ]; then
    mavenAarByType ad
fi

echo "是否需要修改iap 版本:（yes/no or Y/N）"
read -r need_update_iap_version
echo "iap 版本${need_update_iap_version}"
if [ "${need_update_iap_version}" == "yes" ]  || [ "${need_update_iap_version}" == "Y" ]; then
     mavenAarByType iap
fi


git push origin master
git push --tags