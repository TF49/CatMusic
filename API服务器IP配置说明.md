# API服务器IP配置说明

## 📋 文档概述

本文档详细说明了CatMusic项目中需要配置API服务器IP地址的所有位置，以及应对网络IP变化的解决方案。

## 🔧 需要修改IP地址的位置


### 1. Android应用配置类

**文件路径**: `app/src/main/java/com/example/catmusic/Config.java`

```java
// 第10行：服务器基础URL
public static final String BASE_URL = "http://172.18.15.220:3000/";
```

**修改说明**: 将 `172.18.15.220` 替换为当前网络的实际IP地址

### 2. 直接使用IP地址的Activity文件

以下文件中也直接使用了IP地址，需要同步修改：

#### PlayerActivity.java
- **位置**: 第251行附近
- **代码**: `String url = "http://172.18.15.220:3000/api/getSongsUrl?" + midUrls.toString();`

#### SongListActivity.java  
- **位置**: 第227行附近
- **代码**: `String url = "http://172.18.15.220:3000/api/getSongsUrl?" + midUrls.toString();`

#### HomeActivity.java
- **位置**: 第94行附近
- **代码**: `.url("http://172.18.15.220:3000/api/getRecommend")`

## 🚀 快速修改脚本

### Windows PowerShell 脚本

创建 `update_ip.ps1` 文件：

```powershell
# 获取当前IP地址
$currentIP = (Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias "以太网" | Where-Object {$_.IPAddress -like "192.168.*" -or $_.IPAddress -like "172.*"}).IPAddress

if ($currentIP) {
    Write-Host "当前IP地址: $currentIP"
    
    # 修改服务器配置文件
    (Get-Content "catmusic_server-main/prod.server.js") | 
        ForEach-Object { $_ -replace "172\.18\.15\.220", $currentIP } | 
        Set-Content "catmusic_server-main/prod.server.js"
    
    # 修改Android配置
    (Get-Content "app/src/main/java/com/example/catmusic/Config.java") | 
        ForEach-Object { $_ -replace "172\.18\.15\.220", $currentIP } | 
        Set-Content "app/src/main/java/com/example/catmusic/Config.java"
    
    Write-Host "IP地址更新完成!"
} else {
    Write-Host "无法获取当前IP地址"
}
```

### 手动修改步骤

1. **获取当前IP地址**:
   ```powershell
   ipconfig | findstr "IPv4"
   ```

2. **批量替换所有文件**:
   ```powershell
   # 替换服务器文件
   (Get-Content "catmusic_server-main/prod.server.js") -replace '172\.18\.15\.220', '新的IP地址' | Set-Content "catmusic_server-main/prod.server.js"
   
   # 替换Android配置文件
   (Get-Content "app/src/main/java/com/example/catmusic/Config.java") -replace '172\.18\.15\.220', '新的IP地址' | Set-Content "app/src/main/java/com/example/catmusic/Config.java"
   ```

## 💡 最佳实践建议

### 方案1：使用动态DNS服务
- 注册动态DNS服务（如花生壳、No-IP）
- 使用域名代替IP地址
- 配置自动更新脚本

### 方案2：本地网络配置
- 在路由器中设置静态IP地址分配
- 为开发设备分配固定IP
- 避免IP地址频繁变化

### 方案3：开发环境优化
- 使用 `localhost` 进行本地开发
- 仅在需要外部访问时使用实际IP
- 创建开发/生产环境配置分离

## 🔍 验证步骤

修改IP地址后，请按以下步骤验证：

1. **重启服务器**:
   ```bash
   cd catmusic_server-main
   node prod.server.js
   ```

2. **测试API连接**:
   ```powershell
   Invoke-WebRequest -Uri "http://新IP:3000/api/getLyric?mid=0039MnYb0qxYhV" -Method Get
   ```

3. **检查Android应用**:
   - 重新编译Android应用
   - 测试所有网络功能

## 📞 故障排除

### 常见问题

1. **服务器无法启动**:
   - 检查IP地址格式是否正确
   - 确认端口3000未被占用
   - 查看防火墙设置

2. **Android应用无法连接**:
   - 确认设备与服务器在同一网络
   - 检查Android网络权限
   - 验证URL地址拼写

3. **API返回错误**:
   - 检查服务器日志
   - 验证API路径是否正确
   - 确认第三方服务可用性

### 日志检查

查看服务器运行状态：
```bash
# 检查服务器进程
netstat -ano | findstr :3000

# 查看服务器日志
# 在服务器启动终端中查看实时日志
```

## 📚 相关文件清单

需要修改IP地址的文件列表：
- `catmusic_server-main/prod.server.js`
- `app/src/main/java/com/example/catmusic/Config.java`
- `app/src/main/java/com/example/catmusic/ui/activity/PlayerActivity.java`
- `app/src/main/java/com/example/catmusic/ui/activity/SongListActivity.java`
- `app/src/main/java/com/example/catmusic/ui/activity/HomeActivity.java`

---

**最后更新**: 2024年
**维护者**: CatMusic开发团队