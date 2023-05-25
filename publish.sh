#参考readme
set -e
dtsdkCoreVersionName=$(cat -v build.gradle.kts | grep "dtsdkCoreVersionName")
dtsdkCoreVersionName=$(echo "${dtsdkCoreVersionName}" |awk -F "," '{print $2}' |awk -F "\"" '{print $2}')
echo ${dtsdkCoreVersionName}

cd roiquery-core
gradle clean
gradle assemblePublicRelease
gradle copyProguardMappingFiles

git add build.gradle.kts
git add proguard-mapping
git commit -m "Bump version to '$dtsdkCoreVersionName'." # 'dtsdkCoreVersionName' 替换掉之前记录的文本, 下同。

git tag "core/$dtsdkCoreVersionName"
git push --tags

gradle publish
