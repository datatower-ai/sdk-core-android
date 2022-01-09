#!/bin/bash



modifyDependenceType(){
  #获取当前 maven 仓库的type
  dependence_type=$(cat -v ./build.gradle | grep ext.dependence_type |awk -F "=" '{print $2}')


  # 修改maven 仓库为远程模式
  if [ $dependence_type != "\"2\"" ]; then
    sed -i.bak "s/ext.dependence_type.*=.*$dependence_type/ext.dependence_type = \"2\"/g" build.gradle
    rm -rf build.gradle.bak
    if [ $type = 1 ]; then
      git commit build.gradle -m "update maven type"
    fi
    if [ $? -ne 0 ]; then
        exit
    fi
  fi
}


modifyVersionName(){
  aar_version_name=$1
  aar_type=$2
  versionName="${aar_type}VersionName"
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
  versionCode="${aar_type}VersionCode"
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
  aar_type=$(echo  ${project_name} | awk -F "-"  '{print $2}')
  echo ---------------------${aar_type}  start --------------------------

  modifyVersionName "${aar_version_name}"  "${aar_type}"
  modifyVersionCode  "${aar_version_code}" "${aar_type}"

    # shellcheck disable=SC2164
  cd roiquery-"${aar_type}"
  gradle clean
  if [ $type = 0 ]; then
      gradle assembleRelease
  else
    gradle publish
  fi
    if [  $? -ne 0 ]; then
        exit
    fi
  cd ..
  if [ "$type" = 1 ]; then
    git commit  conf.gradle -m "update ${aar_type} versionName ${aar_version_name}, versionCode ${aar_version_code}"
    git tag "${aar_type}"/"${aar_version_name}"
  fi

  if [  $? -ne 0 ]; then
      exit
  fi
}
copyMappingFile(){
  parent_path=${project_name}/build/outputs/mapping/
  if [ -d ${parent_path}release ];then
    echo -------------------copyMappingFile------------------------
    mkdir ${parent_path}${aar_version_name}
    cp ${parent_path}release/* ${parent_path}${aar_version_name}
    rm -rf ${parent_path}release
  fi
  echo ---------------------${aar_type}  end --------------------------
}

aar_version_name=$1
aar_version_code=$2
type=$3
project_name=$4
echo $0 === $1 ==== $2 == $3 == $4
modifyDependenceType
if [  $? -ne 0 ]; then
    exit
fi
mavenAarByType core
copyMappingFile
if [ "$type" = 1 ]; then
  git push origin master
  git push --tags
fi


