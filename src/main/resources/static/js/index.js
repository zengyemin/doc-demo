//分片大小 10m
const chunkSize = 5 * 1024 * 1024;

uploadFile = async () => {
  //获取用户选择的文件
  const file = document.getElementById('selectFile').files[0];
  if (file == null) {
    alert("请选择上传文件");
    return;
  }
  //文件大小(大于5m再分片哦，否则直接走普通文件上传的逻辑就可以了，这里只实现分片上传逻辑)
  const fileSize = file.size;

  //计算当前选择文件需要的分片数量
  const chunkCount = Math.ceil(fileSize / chunkSize)
  console.log('文件大小：', file.size / 1024 / 1024 + 'Mb', '分片数：', chunkCount)

  //获取文件md5
  const fileMd5 = await getFileMd5ByChunk(file)
  console.log('文件md5：', fileMd5)

  console.log('向后端请求本次分片上传初始化');
  let uploadType = $("input[name='uploadType']:checked").val();
  console.log(uploadType);
  if (uploadType === 'minio') {
    minioUploadHandler(file, fileMd5, chunkCount);
  } else {
    localUploadHandler(file, fileMd5, chunkCount);
  }

};

minioUploadHandler = async (file, fileMd5, chunkCount) => {
  const fileSize = file.size;
  const data = await minioUpload(file.name, fileMd5, chunkCount);
  console.log('开始进行分段上传');
  const uploadUrlList = data.uploadUrlList;//创建的url集合
  //已经上传的数量
  const useUploadNum = data.useUploadNum;

  let load = document.getElementById('progress');

  for (let i = 0; i < uploadUrlList.length; i++) {
    let chunkIndex = i + 1;
    //如果当前执行上传的url下标小于已上传的下标则跳过上传
    if (i < useUploadNum) {
      console.log("第 " + chunkIndex + " 分断文件已经上传过，本次上传将忽略");
      continue;
    }

    if (i === 12) {
      return;
    }
    //分片开始位置
    let start = i * chunkSize;
    //分片结束位置
    let end = Math.min(fileSize, start + chunkSize);
    //取文件指定范围内的byte，从而得到分片数据
    let chunkFile = file.slice(start, end);
    //获取执行路径
    let url = uploadUrlList[i];
    console.log("开始上传第 " + chunkIndex + " 个分段数据，文件大小：" + chunkFile.size);
    await uploadChunk(url, chunkFile);
    let progress = Math.round(chunkIndex / uploadUrlList.length * 100);
    load.innerText = progress + '%';
    load.style.width = progress + '%';
    console.log("第 " + chunkIndex + " 个分段数据上传完毕");
  }
  console.log("分段上传完毕，调用接口进行合并");
  const mergeData = await minioUpload(file.name, fileMd5, chunkCount);
  console.log(mergeData);
};

minioUpload = (fileName, fileMd5, chunkCount) => {
  let tempRes = null;
  $.ajax({
    type: "POST",
    url: 'http://localhost:8080/multiPart/minioUpload',
    ContentType: 'application/json',
    data: {fileName: fileName, fileMd5: fileMd5, chunkCount: chunkCount},
    async: false, // 同步
    success: function (res) {
      tempRes = res;
    }
  });
  return tempRes;
};

localUploadHandler = async (file, fileMd5, chunkCount) => {
  const fileSize = file.size;
  // let result = document.getElementById('progress');
  let load = document.getElementById('progress');
  let data = await localUpload(file.name, fileMd5, chunkCount, null);
  if (data.statusEnum === "上传完毕") {
    load.innerText = 100 + '%';
    load.style.width = 100 + '%';
    alert("当前文件已经上传完毕");
  }
  //已经上传的数量
  const useUploadNum = data.useUploadNum;
  console.log(data);
  for (let i = 0; i < chunkCount; i++) {
    let chunkIndex = i + 1;
    //如果当前执行上传的url下标小于已上传的下标则跳过上传
    if (i < useUploadNum) {
      console.log("第 " + chunkIndex + " 分断文件已经上传过，本次上传将忽略");
      continue;
    }
    if (i === 12) {
      return;
    }
    //分片开始位置
    let start = i * chunkSize;
    //分片结束位置
    let end = Math.min(fileSize, start + chunkSize);
    //取文件指定范围内的byte，从而得到分片数据
    let chunkFile = file.slice(start, end);
    console.log("开始上传第 " + chunkIndex + " 个分段数据，文件大小：" + chunkFile.size);
    await localUpload(file.name, fileMd5, chunkCount, chunkFile);
    let progress = Math.round(chunkIndex / chunkCount * 100);
    load.style.width = progress + '%';
    load.innerText = progress + '%';
  }
  let endData = await localUpload(file.name, fileMd5, chunkCount, null);
  console.log(endData);
};

localUpload = (fileName, fileMd5, chunkCount, chunkFile) => {
  let formData = new FormData()
  formData.append("file", chunkFile);
  formData.append("fileName", fileName);
  formData.append("fileMd5", fileMd5);
  formData.append("chunkCount", chunkCount);

  let data = null;
  $.ajax({
    type: "POST",
    url: 'http://localhost:8080/multiPart/localUpload',
    data: formData,
    cache: false, // 上传文件无需缓存
    processData: false, // 使数据不做处理
    contentType: false, // 不要设置Content-Type请求头
    async: false, // 同步
    success: function (res) {
      data = res;
    }
  });
  return data;
};

/**
 * 测试视频播放
 * @param url
 * @param suffix
 */
videoPlay = (url, suffix) => {
  if (suffix === '.mp4') {
    // 方便测试用
    let video = document.getElementById('video')
    video.src = url
    video.load()
  }
};

getFileMd5ByChunk = (file) => {
  let blobSlice = File.prototype.slice || File.prototype.mozSlice
      || File.prototype.webkitSlice,
      chunks = Math.ceil(file.size / chunkSize),
      currentChunk = 0,
      spark = new SparkMD5.ArrayBuffer(),
      fileReader = new FileReader()

  return new Promise((resolve) => {
    fileReader.onload = function (e) {
      spark.append(e.target.result) // Append array buffer
      currentChunk++

      if (currentChunk < chunks) {
        loadNext()
      } else {
        let fileMd5 = spark.end()
        console.info('computed hash', fileMd5) // Compute hash
        resolve(fileMd5)
      }
    };

    fileReader.onerror = function () {
      console.warn('oops, something went wrong.')
    };

    function loadNext() {
      let start = currentChunk * chunkSize,
          end = start + chunkSize >= file.size ? file.size : start + chunkSize

      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }

    loadNext()
  })
};

uploadChunk = (chunkUploadUrl, chunkFile) => {
  return axios.put(chunkUploadUrl, chunkFile)
};



