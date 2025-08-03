# Prescription Management System API 文档

## 用户模块

### 注册
- **POST** `/api/auth/register`
- **请求体**（JSON）:
  ```json
  {
    "username": "string",
    "password": "string",
    "realName": "string",
    "role": "admin|doctor|pharmacist"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "msg": "注册成功"
  }
  ```

### 登录
- **POST** `/api/auth/login`
- **请求体**（JSON）:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "token": "jwt-token",
    "role": "admin|doctor|pharmacist",
    "username": "string",
    "menu": ["菜单1", "菜单2"],
    "expire": 1719999999999
  }
  ```

### 获取当前用户信息
- **GET** `/api/auth/me`
- **Header**: `Authorization: Bearer <token>`
- **响应**:
  ```json
  {
    "username": "string",
    "role": "admin|doctor|pharmacist",
    "menu": ["菜单1", "菜单2"]
  }
  ```

### 刷新Token
- **POST** `/api/auth/refresh-token`
- **Header**: `Authorization: Bearer <token>`
- **响应**:
  ```json
  {
    "token": "new-jwt-token",
    "expire": 1719999999999
  }
  ```

---

## 药品模块

### 新增药品
- **POST** `/api/drugs`
- **请求体**（JSON）:
  ```json
  {
    "drugName": "string",
    "specification": "string",
    "manufacturer": "string",
    "indications": "string"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "msg": "新增成功"
  }
  ```

### 修改药品
- **PUT** `/api/drugs/{id}`
- **请求体**（JSON）: 同上
- **响应**: 同上

### 删除药品
- **DELETE** `/api/drugs/{id}`
- **响应**:
  ```json
  {
    "success": true,
    "msg": "删除成功"
  }
  ```

### 查询药品详情
- **GET** `/api/drugs/{id}`
- **响应**:
  ```json
  {
    "id": 1,
    "drugName": "string",
    "specification": "string",
    "manufacturer": "string",
    "indications": "string",
    "createdTime": "2025-06-28T12:00:00",
    "createdBy": 1,
    "updatedTime": "2025-06-28T12:00:00",
    "updatedBy": 1
  }
  ```

### 分页查询药品
- **GET** `/api/drugs?page=1&size=20&name=xxx`
- **响应**:
  ```json
  {
    "list": [ ... ],
    "total": 100
  }
  ```

### 药品名称唯一性校验
- **GET** `/api/drugs/check-name?name=xxx`
- **响应**:
  ```json
  {
    "unique": true
  }
  ```

### 上传药品图片
- **POST** `/api/drugs/{id}/images`
- **form-data**:
  - files: 多文件
  - imageType: string
- **响应**:
  ```json
  {
    "success": true,
    "paths": ["/upload/drug/xxx.jpg"],
    "msg": "上传成功"
  }
  ```

### 删除药品图片
- **DELETE** `/api/drugs/images/{imageId}`
- **响应**:
  ```json
  {
    "success": true,
    "msg": "删除成功"
  }
  ```

---

## 药方模块

### 新增药方
- **POST** `/api/prescriptions`
- **请求体**（JSON）:
  ```json
  {
    "prescriptionName": "string",
    "indications": "string",
    "usage": "string",
    "treatmentCycle": "string",
    "createdBy": 1
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "msg": "新增成功"
  }
  ```

### 分页查询药方
- **GET** `/api/prescriptions?page=1&size=20&name=xxx&indications=xxx`
- **响应**:
  ```json
  {
    "list": [ ... ],
    "total": 100
  }
  ```

### 获取药方详情
- **GET** `/api/prescriptions/{id}`
- **响应**:
  ```json
  {
    "id": 1,
    "prescriptionName": "string",
    "indications": "string",
    "usage": "string",
    "treatmentCycle": "string",
    "createdTime": "2025-06-28T12:00:00",
    "createdBy": 1,
    "updatedTime": "2025-06-28T12:00:00",
    "updatedBy": 1
  }
  ```

### 修改药方
- **PUT** `/api/prescriptions/{id}`
- **请求体**（JSON）: 同新增
- **响应**: 同新增

### 删除药方（批量）
- **DELETE** `/api/prescriptions`
- **请求体**（JSON）:
  ```json
  [1, 2, 3]
  ```
- **响应**:
  ```json
  {
    "success": true,
    "msg": "删除成功"
  }
  ```

### 上传药方图片
- **POST** `/api/prescriptions/{id}/images`
- **form-data**:
  - files: 多文件
  - imageType: string
- **响应**:
  ```json
  {
    "success": true,
    "paths": ["/upload/prescription/xxx.jpg"],
    "msg": "上传成功"
  }
  ```
