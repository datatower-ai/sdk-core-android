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
    if [ $# == 0 ]; then
        exit
    fi
  fi
}


modifyVersionName(){
  aar_version_name=$1
  aar_type=$2
  echo "${aar_version_name}"
  echo "${aar_type}"
  versionName=("${aar_type}VersionName")
  #获取aar 版本名称
  # shellcheck disable=SC2128
  version_name=$(cat -v ./conf.gradle | grep  "${versionName}")
  old_version_name=$(echo "${version_name##*versionName}" |awk -F ":" '{print $2}' |awk -F "," '{print $1}'|awk -F "'" '{print $2}')

  echo "version_name: ${version_name}********old_version_name:${old_version_name}"
  #比较输入的版本名称，不同则修改
  if [ "${aar_version_name}" != "${old_version_name}" ]; then
      sed -i.bak "s/${version_name}/\t\t\t${versionName}      :'${aar_version_name}',/g" conf.gradle
      rm -rf conf.gradle.bak
  fi

}


modifyVersionCode(){
  aar_version_code=$1
  aar_type=$2
  echo "${aar_version_code}"
  echo "${aar_type}"
  versionCode=("${aar_type}VersionCode")
  #获取aar 版本号
  version_code=$(cat -v ./conf.gradle | grep  "${versionCode}")
  old_version_code=$(echo "${version_code##*versionCode}" |awk -F ":" '{print $2}' |awk -F "," '{print $1}')


  #比较输入的版本号，不同则修改
  if [ "${aar_version_code}" != "${old_version_code}" ]; then
      sed -i.bak "s/${old_version_code}/ ${aar_version_code}/g" conf.gradle
      sed -i.bak "s/${version_code}/\t\t\t${versionCode}      :${aar_version_code},/g" conf.gradle
      rm -rf conf.gradle.bak
  fi

}

modifyDependenceType
git pull
if [  $# == 0 ]; then
    exit
fi
echo "是否需要修改core 版本:（yes/no or Y/N)"
read -r need_update_core_version

if [ "${need_update_core_version}" == "yes" ]  || [ "${need_update_core_version}" == "Y" ]; then
    echo "YES-----------"

    echo  "请输入core aar 版本名称(eg: 1.0.25):"
    read aar_version_name

    echo  "请输入core 版本号:"
    read aar_version_code

    modifyVersionName "${aar_version_name}"  core
    modifyVersionCode  "${aar_version_code}" core

    # shellcheck disable=SC2164
    cd roiquery-core
    gradle clean
    gradle publish

    if [  $# == 0 ]; then
        exit
    fi
    cd ..

    git commit  conf.gradle -m "update core versionName ${aar_version_name}, versionCode ${aar_version_code}"
    git tag core/${aar_version_name}
fi


echo "是否需要修改ad 版本:（yes/no or Y/N）"
read -r need_update_ad_version

if [ "${need_update_ad_version}" == "yes" ]  || [ "${need_update_ad_version}" == "Y" ]; then
    echo "YES-----------"

    echo  "请输入ad aar 版本名称(eg: 1.0.25):"
    read aar_version_name

    echo  "请输入ad 版本号:"
    read aar_version_code

    modifyVersionName "${aar_version_name}"  ad
    modifyVersionCode  "${aar_version_code}" ad

    # shellcheck disable=SC2164
    cd roiquery-ad
    gradle clean
    gradle publish
    if [  $# == 0 ]; then
        exit
    fi
    cd ..
    git commit conf.gradle -m "update ad versionName ${aar_version_name}, versionCode ${aar_version_code}"
    git tag ad/${aar_version_name}
fi



echo "是否需要修改iap 版本:（yes/no or Y/N）"
read -r need_update_iap_version
echo "iap 版本${need_update_iap_version}"

if [ "${need_update_iap_version}" == "yes" ]  || [ "${need_update_iap_version}" == "Y" ]; then
    echo "YES-----------"

    echo  "请输入iap aar 版本名称(eg: 1.0.25):"
    read aar_version_name

    echo  "请输入iap 版本号:"
    read aar_version_code

    modifyVersionName "${aar_version_name}"  iap
    modifyVersionCode  "${aar_version_code}" iap

    # shellcheck disable=SC2164
    cd roiquery-iap
    gradle clean
    gradle publish
    if [  $# == 0 ]; then
        exit
    fi
    cd ..
    git commit conf.gradle -m "update iap versionName ${aar_version_name}, versionCode ${aar_version_code}"
    git tag iap/${aar_version_name}
fi



echo "是否需要修改auth 版本:（yes/no or Y/N）"
read -r need_update_auth_version

if [ "${need_update_auth_version}" == "yes" ]  || [ "${need_update_auth_version}" == "Y" ]; then
    echo "YES-----------"

    echo  "请输入auth aar 版本名称(eg: 1.0.25):"
    read aar_version_name

    echo  "请输入auth 版本号:"
    read aar_version_code

    modifyVersionName "${aar_version_name}"  auth
    modifyVersionCode  "${aar_version_code}" auth

    # shellcheck disable=SC2164
    cd roiquery-auth
    gradle clean
    gradle publish
    
    if [  $# == 0 ]; then
        exit
    fi
    cd ..

    git commit conf.gradle -m "update auth versionName ${aar_version_name}, versionCode ${aar_version_code}"
    git tag auth/${aar_version_name}
fi
git push origin --all