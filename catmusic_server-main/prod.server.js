const express = require("express");
const compression = require("compression");
const cookieParser = require("cookie-parser");
const csrf = require("xsrf");
const path = require("path");
const registerRouter = require("./router");

const port = process.env.PORT || 3000;

const app = express();
//设置允许跨域访问该服务.
app.all("*", function (req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Content-Type");
  res.header("Access-Control-Allow-Methods", "*");
  res.header("Content-Type", "application/json;charset=utf-8");
  next();
});

// const csrfProtection = csrf({
//   cookie: true,
//   ignoreMethods: ["HEAD", "OPTIONS"],
//   checkPathReg: /^\/api/,
// });
app.use(cookieParser());
// app.use(csrfProtection);

app.get("/", function (req, res, next) {
  res.cookie("XSRF-TOKEN", req.csrfToken());
  return next();
});

registerRouter(app);

// 提供自定义歌曲音频文件的静态访问
// 将 mp3 放在 catmusic_server-main/public/music 目录下
// 访问 URL 如：http://<服务器IP>:3000/music/xxx.mp3
app.use(
  "/music",
  express.static(path.join(__dirname, "public", "music"))
);

// 提供自定义封面图片的静态访问
// 建议将封面图放在 catmusic_server-main/public/images 目录下
// 访问 URL 如：http://<服务器IP>:3000/images/xxx.jpg
app.use(
  "/images",
  express.static(path.join(__dirname, "public", "images"))
);

app.use(compression());

// 前端页面静态资源
app.use(express.static("./dist"));

app.use(function (err, req, res, next) {
  if (err.code !== "EBADCSRFTOKEN") {
    return next();
  }

  // handle CSRF token errors here
  res.status(403);
  res.send(
    "<p>接口已经被我用 CSRF 保护了，请参考课程用自己的服务器代理接口</p>"
  );
});

module.exports = app.listen(port, function (err) {
  if (err) {
    console.log(err);
    return;
  }
  console.log("Listening at http://localhost:" + port + "\n");
});
