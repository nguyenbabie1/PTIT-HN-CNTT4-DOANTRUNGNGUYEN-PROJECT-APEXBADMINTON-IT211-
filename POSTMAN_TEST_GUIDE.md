# Hướng dẫn sử dụng Postman Collection

## ApexBadminton – Chi tiết Test Case theo từng chức năng

> Phiên bản: 1.0
> Cập nhật: 2026-06-14
> Ứng dụng: ApexBadminton – Hệ thống đặt lịch và quản lý sân cầu lông
> Công nghệ: Spring Boot 4.0.6, Java 17, MySQL, JWT, Cloudinary

---

## Mục lục

1. [FR-01 – Đăng nhập hệ thống (Cấp phát JWT)](#fr-01--đăng-nhập-hệ-thống-cấp-phát-jwt)
2. [FR-02 – Xoay vòng Token (Refresh Token)](#fr-02--xoay-vòng-token-refresh-token)
3. [FR-03 – Đăng xuất (Revoke Token)](#fr-03--đăng-xuất-revoke-token)
4. [FR-04 – Đăng ký tài khoản người dùng](#fr-04--đăng-ký-tài-khoản-người-dùng)
5. [FR-05 – Quản lý Người dùng (CRUD, Tìm kiếm, Phân trang)](#fr-05--quản-lý-người-dùng-crud-tìm-kiếm-phân-trang)
6. [FR-06 – Đặt lịch đánh cầu](#fr-06--đặt-lịch-đánh-cầu)
7. [FR-07 – Xem lịch sử đặt hàng](#fr-07--xem-lịch-sử-đặt-hàng)
8. [FR-08 – Phê duyệt / Từ chối lịch](#fr-08--phê-duyệt--từ-chối-lịch)
9. [FR-09 – Tải lên và lưu trữ nhiều hình ảnh sân cầu](#fr-09--tải-lên-và-lưu-trữ-nhiều-hình-ảnh-sân-cầu)
10. [FR-10 – Đổi mật khẩu / Quên mật khẩu](#fr-10--đổi-mật-khẩu--quên-mật-khẩu)
11. [FR-11 – Ghi log thời gian thực hiện (AOP Logging)](#fr-11--ghi-log-thời-gian-thực-hiện-cho-tất-cả-các-chức-năng-aop-logging)

---

## Tổng quan môi trường test

| Thành phần | Yêu cầu | Ghi chú |
|-----------|---------|---------|
| JDK | Java 17 | Dùng `java -version` kiểm tra |
| MySQL | 8.0+ | Chạy tại `localhost:3306` |
| Database | `apexbadminton_DB` | Tự động tạo nếu chưa có |
| Gradle | 8+ hoặc dùng `./gradlew` | |
| Port ứng dụng | 8080 | Không bị process khác chiếm |
| Cloudinary | Có env vars nếu test upload ảnh thật | Không cần nếu mock |

### Khởi động ứng dụng

```bash
./gradlew bootRun
```

Kiểm tra ứng dụng đã chạy:
```bash
curl http://localhost:8080/api/v1/courts
```

---

## FR-01 — Đăng nhập hệ thống (Cấp phát JWT)

### Tóm tắt

Chức năng đăng nhập xác thực người dùng và cấp phát cặp JWT (Access Token + Refresh Token) để sử dụng cho các request tiếp theo.

### Luồng thực hiện

```
Client → AuthController (POST /api/v1/auth/login, tham số LoginRequest)
      → AuthService.login()
      → AuthenticationManager.authenticate() với UsernamePasswordAuthenticationToken
      → CustomUserDetailsService.loadUserByUsername()
      → UserRepository
      → JwtService.generateAccessToken() + generateRefreshToken()
      → Trả về AuthResponse
```

### Điều kiện để chạy test

1. **Database**
   - User đã tồn tại trong DB (đã register hoặc seed data)
   - Trường `password` đã được mã hóa BCrypt
   - Bảng `users` đã được tạo (tự động tạo bởi `ddl-auto=update`)

2. **Test data mẫu**
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR01-TC01 | Đăng nhập thành công | `{ "username": "admin", "password": "admin123" }` | `200 OK`, có `accessToken`, `refreshToken`, `tokenType: "Bearer"` |
| FR01-TC02 | Sai password | `{ "username": "admin", "password": "wrong" }` | `401 Unauthorized` |
| FR01-TC03 | User không tồn tại | `{ "username": "nouser", "password": "any" }` | `401 Unauthorized` |
| FR01-TC04 | Thiếu username | `{ "password": "admin123" }` | `400 Bad Request` |
| FR01-TC05 | Thiếu password | `{ "username": "admin" }` | `400 Bad Request` |
| FR01-TC06 | Username rỗng | `{ "username": "", "password": "admin123" }` | `400 Bad Request` |

### Chi tiết từng bước

#### Bước 1: Đăng nhập thành công (FR01-TC01)

**Mục đích:** Xác thực người dùng và nhận JWT token

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

**Luồng thực hiện:**
1. Client gửi request đăng nhập
2. Controller nhận LoginRequest, validate @Valid
3. AuthService.login() gọi AuthenticationManager.authenticate()
4. Spring Security gọi CustomUserDetailsService.loadUserByUsername()
5. BCryptPasswordEncoder kiểm tra password
6. JwtService.generateAccessToken() tạo Access Token (30 phút)
7. JwtService.generateRefreshToken() tạo Refresh Token (7 ngày)
8. Trả về AuthResponse

**Response mong đợi:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800
}
```

**Postman Setup:**
- Method: `POST`
- URL: `{{baseUrl}}/auth/login`
- Body: Raw JSON như trên
- Test script: Lưu `accessToken` vào collection variable

---

#### Bước 2: Sai password (FR01-TC02)

**Mục đích:** Kiểm tra hệ thống từ chối đăng nhập khi password sai

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "wrongpassword"
}
```

**Expected Result:** `401 Unauthorized`

**Postman Test:**
```javascript
pm.test('Sai password phải trả về 401', function() {
    pm.response.to.have.status(401);
});
```

---

#### Bước 3: User không tồn tại (FR01-TC03)

**Mục đích:** Kiểm tra hệ thống từ chối đăng nhập khi user không tồn tại

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "nonexistent",
    "password": "anypassword"
}
```

**Expected Result:** `401 Unauthorized`

---

#### Bước 4: Thiếu username (FR01-TC04)

**Mục đích:** Kiểm tra validation khi thiếu field bắt buộc

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "password": "admin123"
}
```

**Expected Result:** `400 Bad Request`

**Postman Test:**
```javascript
pm.test('Thiếu username phải trả về 400', function() {
    pm.response.to.have.status(400);
});
```

---

#### Bước 5: Thiếu password (FR01-TC05)

**Mục đích:** Kiểm tra validation khi thiếu field bắt buộc

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "admin"
}
```

**Expected Result:** `400 Bad Request`

---

#### Bước 6: Username rỗng (FR01-TC06)

**Mục đích:** Kiểm tra validation khi field rỗng

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "",
    "password": "admin123"
}
```

**Expected Result:** `400 Bad Request`

---

### Cú pháp gọi test (curl)

```bash
# Test đăng nhập thành công
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Test sai password
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong"}'

# Test thiếu username
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"password":"admin123"}'
```

### Đặc điểm nổi bật

- Mật khẩu không bao giờ được so sánh trực tiếp, luôn qua `BCryptPasswordEncoder`.
- Token được ký bằng HMAC-SHA256 với secret key cấu hình qua `application.properties`.
- Access Token và Refresh Token có thời hạn khác nhau để cân bằng giữa bảo mật và trải nghiệm.

---

## FR-02 — Xoay vòng Token (Refresh Token)

### Tóm tắt

Chức năng làm mới Access Token khi hết hạn mà không yêu cầu người dùng đăng nhập lại, sử dụng Refresh Token còn hiệu lực.

### Luồng thực hiện

```
Client → AuthController (POST /api/v1/auth/refresh, body { refreshToken })
      → AuthService.refresh()
      → JwtService.isRefreshToken() – Kiểm tra là refresh token
      → JwtService.extractUsername() – Lấy username từ token
      → JwtService.isTokenValid() – Kiểm tra token còn hợp lệ
      → JwtService.generateAccessToken() – Tạo access token mới
      → Trả về AuthResponse mới (refresh token cũ vẫn giữ)
```

### Điều kiện để chạy test

1. **Tiền điều kiện**
   - Đã gọi `POST /api/v1/auth/login` và lưu `refreshToken` từ response
   - Refresh token chưa hết hạn (7 ngày)

2. **Test data mẫu**
   - Lấy `refreshToken` từ response của FR01-TC01

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR02-TC01 | Refresh thành công | `{ "refreshToken": "<valid_refresh_token>" }` | `200 OK`, có `accessToken` mới, `refreshToken` cũ |
| FR02-TC02 | Refresh token hết hạn | `{ "refreshToken": "<expired_token>" }` | `401 Unauthorized` |
| FR02-TC03 | Refresh token giả mạo | `{ "refreshToken": "fake-token" }` | `401 Unauthorized` |
| FR02-TC04 | Refresh token rỗng | `{ "refreshToken": "" }` | `400 Bad Request` |
| FR02-TC05 | Refresh token không gửi | `{}` | `400 Bad Request` |

### Chi tiết từng bước

#### Bước 1: Refresh thành công (FR02-TC01)

**Mục đích:** Làm mới Access Token khi hết hạn

**Request:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Luồng thực hiện:**
1. Client gửi refresh token
2. AuthService.refresh() gọi JwtService.isRefreshToken() xác nhận đây là refresh token
3. JwtService.extractUsername() lấy username từ token
4. JwtService.isTokenValid() kiểm tra token còn hạn và chữ ký hợp lệ
5. JwtService.generateAccessToken() tạo Access Token mới (30 phút)
6. Refresh Token cũ vẫn giữ nguyên (không cấp Refresh Token mới)
7. Trả về AuthResponse với accessToken mới

**Response mong đợi:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9... (token mới)",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9... (token cũ giữ nguyên)",
    "tokenType": "Bearer",
    "expiresIn": 1800
}
```

---

#### Bước 2: Refresh token hết hạn (FR02-TC02)

**Mục đích:** Kiểm tra hệ thống từ chối refresh token đã hết hạn

**Request:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAwfQ.expired"
}
```

**Expected Result:** `401 Unauthorized`

**Postman Test:**
```javascript
pm.test('Token hết hạn phải trả về 401', function() {
    pm.response.to.have.status(401);
});
```

---

#### Bước 3: Refresh token giả mạo (FR02-TC03)

**Mục đích:** Kiểm tra hệ thống từ chối token không hợp lệ

**Request:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "fake-token-that-is-not-valid"
}
```

**Expected Result:** `401 Unauthorized`

---

#### Bước 4: Refresh token rỗng (FR02-TC04)

**Mục đích:** Kiểm tra validation khi token rỗng

**Request:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": ""
}
```

**Expected Result:** `400 Bad Request`

---

#### Bước 5: Refresh token không gửi (FR02-TC05)

**Mục đích:** Kiểm tra validation khi không gửi token

**Request:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{}
```

**Expected Result:** `400 Bad Request`

---

### Cú pháp gọi test (curl)

```bash
# Test refresh thành công (thay <REFRESH_TOKEN> bằng token thật)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'

# Test token giả
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"fake-token"}'
```

### Đặc điểm nổi bật

- Không cấp lại Refresh Token mới, giúp phát hiện nếu Refresh Token bị rò rỉ và dùng lại.
- Kiểm tra đồng thời cả chữ ký và thời hạn của token.
- Không yêu cầu xác thực JWT header trong request này vì Access Token đã hết hạn.

---

## FR-03 — Đăng xuất (Revoke Token)

### Tóm tắt

Chức năng đăng xuất vô hiệu hóa Access Token hiện tại bằng cách lưu vào blacklist, đảm bảo token không thể tiếp tục sử dụng dù chưa hết hạn.

### Luồng thực hiện

```
Client (gửi Authorization: Bearer token) → AuthController (POST /api/v1/auth/logout)
      → AuthService.logout()
      → JwtService.isAccessToken() – Kiểm tra là access token
      → JwtService.extractExpiration() – Lấy thời gian hết hạn
      → TokenBlacklistRepository.save() – Lưu vào blacklist
      → Trả về ApiResponse("Logout successful")
```

### Điều kiện để chạy test

1. **Tiền điều kiện**
   - Đã gọi `POST /api/v1/auth/login` và lưu `accessToken` từ response

2. **Test data mẫu**
   - Lấy `accessToken` từ response của FR01-TC01

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR03-TC01 | Đăng xuất thành công | Header `Authorization: Bearer <accessToken>` | `200 OK`, message `"Logout successful"` |
| FR03-TC02 | Đăng xuất không gửi header | Không có header | `400 Bad Request` |
| FR03-TC03 | Dùng lại token sau logout | Header với token đã logout | `401 Unauthorized` |
| FR03-TC04 | Logout với refresh token | Header `Bearer <refreshToken>` | `400 Bad Request` (không phải access token) |
| FR03-TC05 | Token không hợp lệ | Header `Bearer invalid-token` | `400 Bad Request` |

### Chi tiết từng bước

#### Bước 1: Đăng xuất thành công (FR03-TC01)

**Mục đích:** Vô hiệu hóa token hiện tại

**Request:**
```http
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
```

**Luồng thực hiện:**
1. Client gửi request với access token trong header
2. AuthController.extractBearerToken() trích xuất token
3. AuthService.logout() gọi JwtService.isAccessToken() xác nhận đây là access token
4. JwtService.extractExpiration() tính thời gian còn lại của token
5. TokenBlacklistRepository.save() lưu token vào bảng `token_blacklist` với `expiresAt`
6. Trả về message "Logout successful"

**Response mong đợi:**
```json
{
    "success": true,
    "message": "Logout successful"
}
```

**Postman Setup:**
- Method: `POST`
- URL: `{{baseUrl}}/auth/logout`
- Headers: `Authorization: Bearer {{accessToken}}`

---

#### Bước 2: Dùng lại token sau logout (FR03-TC03)

**Mục đích:** Kiểm tra token đã logout không thể sử dụng

**Request:**
```http
GET /api/v1/bookings/me
Authorization: Bearer <token_đã_logout>
```

**Expected Result:** `401 Unauthorized`

**Postman Test:**
```javascript
pm.test('Token đã logout không thể sử dụng', function() {
    pm.response.to.have.status(401);
});
```

---

#### Bước 3: Logout với refresh token (FR03-TC04)

**Mục đích:** Kiểm tra hệ thống từ chối refresh token

**Request:**
```http
POST /api/v1/auth/logout
Authorization: Bearer <refreshToken>
```

**Expected Result:** `400 Bad Request`

---

### Cú pháp gọi test (curl)

```bash
# Test đăng xuất thành công (thay <ACCESS_TOKEN> bằng token thật)
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Test dùng lại token sau logout (phải trả về 401)
curl -X GET http://localhost:8080/api/v1/bookings/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### Đặc điểm nổi bật

- Token bị blacklist không thể sử dụng cho bất kỳ request nào.
- Token tự động hết hiệu lực khi đến `expiresAt` (có thể dọn dẹp định kỳ).
- Kiểm tra blacklist được tích hợp vào `JwtAuthenticationFilter` cho mọi request.

---

## FR-04 — Đăng ký tài khoản người dùng

### Tóm tắt

Chức năng đăng ký cho phép người dùng mới tạo tài khoản với quyền CUSTOMER được gán tự động, không cần quản trị viên can thiệp.

### Luồng thực hiện

```
Client → UserController (POST /api/v1/users, tham số UserPostRequest)
      → UserService.register()
      → UserRepository.existsByUsername()
      → UserRepository.existsByEmail()
      → PasswordEncoder.encode()
      → UserRepository.save()
      → Trả về UserResponse
```

### Điều kiện để chạy test

1. **Database**
   - DB đang chạy, bảng `users` đã được tạo
   - Không có user nào trùng `username` hoặc `email` cần đăng ký

2. **Test data mẫu**
   ```json
   {
     "username": "newuser",
     "email": "newuser@apex.com",
     "password": "newuser123",
     "fullName": "New User"
   }
   ```

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR04-TC01 | Đăng ký thành công | `{ "username":"newuser","email":"new@apex.com","password":"newuser123","fullName":"New User" }` | `201 Created`, có user info, role = CUSTOMER |
| FR04-TC02 | Email trùng | Email đã đăng ký ở TC01 | `409 Conflict` |
| FR04-TC03 | Username trùng | Username đã đăng ký ở TC01 | `409 Conflict` |
| FR04-TC04 | Password yếu (ngắn) | `{ "password":"123" }` | `400 Bad Request` |
| FR04-TC05 | Password yếu (không có ký tự đặc biệt) | `{ "password":"newuser123" }` | `400 Bad Request` |
| FR04-TC06 | Email không đúng định dạng | `{ "email":"not-an-email" }` | `400 Bad Request` |
| FR04-TC07 | Thiếu username | Thiếu field `username` | `400 Bad Request` |
| FR04-TC08 | Thiếu email | Thiếu field `email` | `400 Bad Request` |

### Chi tiết từng bước

#### Bước 1: Đăng ký thành công (FR04-TC01)

**Mục đích:** Tạo tài khoản mới với quyền CUSTOMER

**Request:**
```http
POST /api/v1/users
Content-Type: application/json

{
    "username": "newuser",
    "email": "newuser@apex.com",
    "password": "newuser123",
    "fullName": "New User"
}
```

**Luồng thực hiện:**
1. Client gửi request đăng ký
2. Controller nhận UserPostRequest, validate @Valid
3. Kiểm tra `username` đã tồn tại chưa qua UserRepository.existsByUsername()
4. Kiểm tra `email` đã tồn tại chưa qua UserRepository.existsByEmail()
5. Nếu trùng, ném ConflictException → trả về 409
6. Mã hóa password bằng BCryptPasswordEncoder
7. Tạo đối tượng User với enabled=true, gán role ROLE_CUSTOMER
8. Lưu vào database qua UserRepository.save()

**Response mong đợi:**
```json
{
    "id": 1,
    "username": "newuser",
    "email": "newuser@apex.com",
    "fullName": "New User",
    "role": "ROLE_CUSTOMER",
    "enabled": true
}
```

**Postman Test:**
```javascript
pm.test('Đăng ký thành công', function() {
    pm.response.to.have.status(201);
    var jsonData = pm.response.json();
    pm.expect(jsonData.role).to.equal('ROLE_CUSTOMER');
});
```

---

#### Bước 2: Email trùng (FR04-TC02)

**Mục đích:** Kiểm tra hệ thống từ chối đăng ký khi email đã tồn tại

**Request:**
```http
POST /api/v1/users
Content-Type: application/json

{
    "username": "anotheruser",
    "email": "newuser@apex.com",
    "password": "another123",
    "fullName": "Another User"
}
```

**Expected Result:** `409 Conflict`

**Postman Test:**
```javascript
pm.test('Email trùng phải trả về 409', function() {
    pm.response.to.have.status(409);
});
```

---

#### Bước 3: Username trùng (FR04-TC03)

**Mục đích:** Kiểm tra hệ thống từ chối đăng ký khi username đã tồn tại

**Request:**
```http
POST /api/v1/users
Content-Type: application/json

{
    "username": "newuser",
    "email": "another@apex.com",
    "password": "another123",
    "fullName": "Another User"
}
```

**Expected Result:** `409 Conflict`

---

#### Bước 4: Password yếu - ngắn (FR04-TC04)

**Mục đích:** Kiểm tra validation password quá ngắn

**Request:**
```http
POST /api/v1/users
Content-Type: application/json

{
    "username": "weakuser",
    "email": "weak@apex.com",
    "password": "123",
    "fullName": "Weak User"
}
```

**Expected Result:** `400 Bad Request`

**Postman Test:**
```javascript
pm.test('Password yếu phải trả về 400', function() {
    pm.response.to.have.status(400);
});
```

---

#### Bước 5: Password yếu - thiếu ký tự đặc biệt (FR04-TC05)

**Mục đích:** Kiểm tra validation password không có ký tự đặc biệt

**Request:**
```http
POST /api/v1/users
Content-Type: application/json

{
    "username": "weakuser2",
    "email": "weak2@apex.com",
    "password": "newuser123",
    "fullName": "Weak User 2"
}
```

**Expected Result:** `400 Bad Request`

---

#### Bước 6: Email không đúng định dạng (FR04-TC06)

**Mục đích:** Kiểm tra validation email

**Request:**
```http
POST /api/v1/users
Content-Type: application/json

{
    "username": "bademail",
    "email": "not-an-email",
    "password": "bademail123",
    "fullName": "Bad Email"
}
```

**Expected Result:** `400 Bad Request`

---

### Cú pháp gọi test (curl)

```bash
# Test đăng ký thành công
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"new@apex.com","password":"newuser123","fullName":"New User"}'

# Test email trùng
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"another","email":"new@apex.com","password":"another123","fullName":"Another"}'

# Test password yếu
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"weak","email":"weak@apex.com","password":"123","fullName":"Weak"}'
```

### Đặc điểm nổi bật

- Endpoint public, không cần JWT token để truy cập.
- Password được mã hóa BCrypt, không bao giờ lưu plain text.
- Người dùng đăng ký mới chỉ được cấp `ROLE_CUSTOMER`, không thể tự đăng ký quyền ADMIN hoặc MANAGER.
- Password không bao giờ xuất hiện trong response.

---

## FR-05 — Quản lý Người dùng (CRUD, Tìm kiếm, Phân trang)

### Tóm tắt

Chức năng quản lý người dùng dành riêng cho ADMIN, cho phép tạo, xem, cập nhật, xóa user và tìm kiếm với phân trang.

### Luồng thực hiện

```
Client (ADMIN) → UserController (/api/v1/users/**)
              → UserService (CRUD + search)
              → UserRepository
              → Database
```

### Điều kiện để chạy test

1. **Phân quyền**
   - Toàn bộ endpoints yêu cầu role `ADMIN`
   - Cần có access token của ADMIN trong header

2. **Database**
   - Ít nhất có 2–3 user để test phân trang

3. **Test data mẫu**
   - ADMIN token: lấy từ FR01-TC01

### Test Case - Tạo user

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR05-TC01 | ADMIN tạo MANAGER thành công | Header ADMIN, `{ "role": "MANAGER", ... }` | `201 Created` |
| FR05-TC02 | ADMIN tạo ADMIN khác | Header ADMIN, `{ "role": "ADMIN", ... }` | `201 Created` |
| FR05-TC03 | Customer tạo ADMIN | Header CUSTOMER, `{ "role": "ADMIN", ... }` | `400 Bad Request` |

### Test Case - Tìm kiếm & Phân trang

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR05-TC04 | Lấy danh sách (phân trang mặc định) | `GET /api/v1/users?page=0&size=10` | `200 OK`, Page với size=10 |
| FR05-TC05 | Tìm theo keyword | `GET /api/v1/users?keyword=admin` | `200 OK`, kết quả chứa keyword |
| FR05-TC06 | Phân trang trang 2 | `GET /api/v1/users?page=1&size=5` | `200 OK`, kết quả trang 2 |
| FR05-TC07 | Sort theo id DESC | `GET /api/v1/users?sort=id,desc` | `200 OK`, kết quả sort đúng |
| FR05-TC08 | Customer truy cập danh sách | Header CUSTOMER | `403 Forbidden` |

### Test Case - Xem chi tiết

| TC-ID | Mô tả | Expected Result |
|-------|-------|-----------------|
| FR05-TC09 | Xem user tồn tại | `200 OK`, đủ thông tin user |
| FR05-TC10 | Xem user không tồn tại (id=99999) | `404 Not Found` |
| FR05-TC11 | Customer xem user | `403 Forbidden` |

### Test Case - Cập nhật

| TC-ID | Mô tả | Expected Result |
|-------|-------|-----------------|
| FR05-TC12 | Cập nhật fullName hợp lệ | `200 OK`, fullName mới được lưu |
| FR05-TC13 | Cập nhật email trùng user khác | `409 Conflict` |
| FR05-TC14 | Cập nhật user không tồn tại | `404 Not Found` |
| FR05-TC15 | Customer cập nhật user | `403 Forbidden` |

### Test Case - Xóa

| TC-ID | Mô tả | Expected Result |
|-------|-------|-----------------|
| FR05-TC16 | Xóa user thành công | `204 No Content` |
| FR05-TC17 | Xóa user không tồn tại | `404 Not Found` |
| FR05-TC18 | Customer xóa user | `403 Forbidden` |

### Chi tiết từng bước

#### Bước 1: Tạo Manager (FR05-TC01)

**Mục đích:** Admin tạo user với role MANAGER

**Request:**
```http
POST /api/v1/users
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
    "username": "manager1",
    "email": "manager1@apex.com",
    "password": "manager123",
    "fullName": "Manager One",
    "role": "MANAGER"
}
```

**Luồng thực hiện:**
1. Client gửi request với ADMIN token
2. Controller nhận UserPostRequest
3. UserService.createUser() kiểm tra username và email chưa tồn tại
4. PasswordEncoder.encode() mã hóa password
5. Tạo User với role MANAGER
6. UserRepository.save() lưu vào DB
7. Trả về UserResponse

**Response mong đợi:**
```json
{
    "id": 2,
    "username": "manager1",
    "email": "manager1@apex.com",
    "fullName": "Manager One",
    "role": "ROLE_MANAGER",
    "enabled": true
}
```

---

#### Bước 2: Phân trang danh sách user (FR05-TC04)

**Mục đích:** Lấy danh sách user với phân trang

**Request:**
```http
GET /api/v1/users?page=0&size=10&sort=id,desc
Authorization: Bearer <ADMIN_TOKEN>
```

**Response mong đợi:**
```json
{
    "content": [...],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0,
    "size": 10
}
```

---

#### Bước 3: Tìm kiếm theo keyword (FR05-TC05)

**Mục đích:** Tìm user theo username, email hoặc fullName

**Request:**
```http
GET /api/v1/users?keyword=admin
Authorization: Bearer <ADMIN_TOKEN>
```

**Luồng thực hiện:**
1. UserService.searchUsers() nhận keyword
2. JPA Specification tạo query với LIKE trên username, fullName, email
3. Tìm kiếm không phân biệt hoa thường
4. Trả về Page<UserResponse>

---

#### Bước 4: Xem chi tiết user (FR05-TC09)

**Mục đích:** Lấy thông tin chi tiết một user

**Request:**
```http
GET /api/v1/users/1
Authorization: Bearer <ADMIN_TOKEN>
```

**Response mong đợi:**
```json
{
    "id": 1,
    "username": "admin",
    "email": "admin@apex.com",
    "fullName": "Admin User",
    "role": "ROLE_ADMIN",
    "enabled": true
}
```

---

#### Bước 5: Cập nhật user (FR05-TC12)

**Mục đích:** Cập nhật thông tin user (partial update)

**Request:**
```http
PUT /api/v1/users/1
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
    "fullName": "Updated Full Name"
}
```

**Luồng thực hiện:**
1. UserService.updateUser() tìm User theo id
2. Chỉ cập nhật field được gửi (fullName)
3. Các field null không thay đổi
4. UserRepository.save() lưu lại

---

#### Bước 6: Xóa user (FR05-TC16)

**Mục đích:** Xóa user khỏi hệ thống

**Request:**
```http
DELETE /api/v1/users/2
Authorization: Bearer <ADMIN_TOKEN>
```

**Expected Result:** `204 No Content`

---

### Cú pháp gọi test (curl)

```bash
# Lấy danh sách user (cần ADMIN token)
curl "http://localhost:8080/api/v1/users?page=0&size=10" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Tìm theo keyword
curl "http://localhost:8080/api/v1/users?keyword=admin" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Tạo MANAGER
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d '{"username":"manager1","email":"manager1@apex.com","password":"manager123","fullName":"Manager One","role":"MANAGER"}'

# Xem chi tiết user
curl http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Cập nhật user
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d '{"fullName":"Updated Name"}'

# Xóa user
curl -X DELETE http://localhost:8080/api/v1/users/3 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

## FR-06 — Đặt lịch đánh cầu

### Tóm tắt

Chức năng cho phép CUSTOMER đặt sân cầu lông theo ngày và khung giờ cụ thể, với kiểm tra tự động để tránh trùng lịch.

### Luồng thực hiện

```
Client (CUSTOMER) → BookingController (POST /api/v1/bookings, tham số BookingRequest)
                  → BookingService.createBooking()
                  → UserService.findUserByUsername()
                  → CourtRepository.findById()
                  → TimeSlotRepository.findById()
                  → BookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn()
                  → BookingRepository.save()
                  → Trả về BookingResponse
```

### Điều kiện để chạy test

1. **Database**
   - Court đã tồn tại và `isActive = true`
   - TimeSlot đã tồn tại
   - Chưa có booking cho (`courtId`, `bookingDate`, `timeSlotId`) với status `PENDING`/`CONFIRMED`

2. **Tiền điều kiện**
   - Đã có CUSTOMER đăng nhập và lấy token
   - Đã tạo Court và TimeSlot (hoặc seed data)

3. **Test data mẫu**
   ```json
   {
     "courtId": 1,
     "timeSlotId": 1,
     "bookingDate": "2026-06-20"
   }
   ```

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR06-TC01 | Đặt lịch thành công | `{ "courtId":1,"timeSlotId":1,"bookingDate":"2026-06-20" }` | `201 Created`, status = PENDING |
| FR06-TC02 | Đặt lịch trùng khung giờ | Gọi lại TC01 với cùng court/date/timeSlot | `409 Conflict` |
| FR06-TC03 | Đặt lịch với sân không tồn tại | `{ "courtId":999, ... }` | `404 Not Found` |
| FR06-TC04 | Đặt lịch với timeSlot không tồn tại | `{ "timeSlotId":999, ... }` | `404 Not Found` |
| FR06-TC05 | Đặt lịch ngày trong quá khứ | `{ "bookingDate":"2020-01-01" }` | `400 Bad Request` |
| FR06-TC06 | Đặt lịch với sân không active | Court có `isActive=false` | `409 Conflict` |
| FR06-TC07 | Customer đặt khi account bị khóa | User có `enabled=false` | `409 Conflict` |
| FR06-TC08 | MANAGER đặt lịch | Header MANAGER | `403 Forbidden` |

### Chi tiết từng bước

#### Bước 1: Đặt lịch thành công (FR06-TC01)

**Mục đích:** Tạo booking mới với status PENDING

**Request:**
```http
POST /api/v1/bookings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
    "courtId": 1,
    "timeSlotId": 1,
    "bookingDate": "2026-06-20"
}
```

**Luồng thực hiện:**
1. Client gửi request với CUSTOMER token
2. Controller nhận BookingRequest, validate @Valid
3. BookingService.createBooking() lấy user từ SecurityContext
4. CourtRepository.findById() kiểm tra court tồn tại và isActive=true
5. TimeSlotRepository.findById() kiểm tra timeSlot tồn tại
6. BookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn() kiểm tra trùng lịch
7. Nếu trùng, ném ConflictException
8. Tạo Booking với status = PENDING
9. BookingRepository.save() lưu vào DB

**Response mong đợi:**
```json
{
    "id": 1,
    "courtId": 1,
    "courtName": "Court 1",
    "timeSlotId": 1,
    "timeSlotLabel": "07:00 - 08:00",
    "bookingDate": "2026-06-20",
    "status": "PENDING",
    "customerName": "Customer User"
}
```

**Postman Test:**
```javascript
pm.test('Đặt lịch thành công', function() {
    pm.response.to.have.status(201);
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.equal('PENDING');
});
pm.collectionVariables.set('bookingId', pm.response.json().id);
```

---

#### Bước 2: Đặt lịch trùng khung giờ (FR06-TC02)

**Mục đích:** Kiểm tra hệ thống từ chối đặt trùng

**Request:**
```http
POST /api/v1/bookings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
    "courtId": 1,
    "timeSlotId": 1,
    "bookingDate": "2026-06-20"
}
```

**Expected Result:** `409 Conflict`

**Postman Test:**
```javascript
pm.test('Trùng khung giờ phải trả về 409', function() {
    pm.response.to.have.status(409);
});
```

---

#### Bước 3: Đặt lịch ngày trong quá khứ (FR06-TC05)

**Mục đích:** Kiểm tra validation ngày không hợp lệ

**Request:**
```http
POST /api/v1/bookings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
    "courtId": 1,
    "timeSlotId": 1,
    "bookingDate": "2020-01-01"
}
```

**Expected Result:** `400 Bad Request`

---

#### Bước 4: Sân không tồn tại (FR06-TC03)

**Mục đích:** Kiểm tra hệ thống từ chối khi sân không tồn tại

**Request:**
```http
POST /api/v1/bookings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
    "courtId": 99999,
    "timeSlotId": 1,
    "bookingDate": "2026-06-20"
}
```

**Expected Result:** `404 Not Found`

---

### Cú pháp gọi test (curl)

```bash
# Test đặt lịch thành công (cần CUSTOMER token)
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <CUSTOMER_TOKEN>" \
  -d '{"courtId":1,"timeSlotId":1,"bookingDate":"2026-06-20"}'

# Test trùng khung giờ
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <CUSTOMER_TOKEN>" \
  -d '{"courtId":1,"timeSlotId":1,"bookingDate":"2026-06-20"}'

# Test ngày quá khứ
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <CUSTOMER_TOKEN>" \
  -d '{"courtId":1,"timeSlotId":1,"bookingDate":"2020-01-01"}'
```

### Đặc điểm nổi bật

- Chống double booking ở 2 tầng: kiểm tra trước khi save + logic kiểm tra status.
- `userId` được lấy tự động từ JWT token, không yêu cầu client truyền userId.
- Chỉ những booking ở status `PENDING` hoặc `CONFIRMED` mới bị trùng.

---

## FR-07 — Xem lịch sử đặt hàng

### Tóm tắt

Chức năng cho phép CUSTOMER xem danh sách lịch đặt sân của mình, và ADMIN/MANAGER xem tất cả booking với khả năng lọc theo ngày và trạng thái.

### Luồng thực hiện

#### Customer xem lịch sử của mình
```
Client (CUSTOMER) → BookingController (GET /api/v1/bookings/me)
                  → BookingService.getMyBookings()
                  → BookingRepository.findByUserId()
                  → Trả về List<BookingResponse>
```

#### Admin/Manager xem tất cả booking
```
Client (ADMIN/MANAGER) → BookingController (GET /api/v1/bookings?date=...&status=...)
                       → ReportService.getBookings()
                       → BookingRepository
                       → Trả về List<BookingResponse>
```

### Điều kiện để chạy test

1. **Database**
   - Customer đã có ít nhất 1 booking (hoặc chưa có cũng được, trả về list rỗng)
   - Có các booking với status khác nhau: `PENDING`, `CONFIRMED`, `REJECTED`

2. **Tiền điều kiện**
   - CUSTOMER đã đăng nhập và có booking
   - ADMIN/MANAGER đã đăng nhập

### Test Case - Customer

| TC-ID | Mô tả | Expected Result |
|-------|-------|-----------------|
| FR07-TC01 | Customer xem lịch của mình | `200 OK`, list đầy đủ booking |
| FR07-TC02 | Customer chưa đặt lịch nào | `200 OK`, list rỗng `[]` |
| FR07-TC03 | MANAGER gọi endpoint customer | `403 Forbidden` |

### Test Case - Admin/Manager

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR07-TC04 | Manager xem tất cả booking | `GET /api/v1/bookings` | `200 OK`, list tất cả booking |
| FR07-TC05 | Lọc theo ngày | `GET /api/v1/bookings?date=2026-06-20` | `200 OK`, chỉ booking ngày đó |
| FR07-TC06 | Lọc theo status | `GET /api/v1/bookings?status=PENDING` | `200 OK`, chỉ PENDING |
| FR07-TC07 | Kết hợp date + status | `GET /api/v1/bookings?date=2026-06-20&status=CONFIRMED` | `200 OK` |
| FR07-TC08 | Customer gọi endpoint manager | Header CUSTOMER | `403 Forbidden` |

### Chi tiết từng bước

#### Bước 1: Customer xem lịch của mình (FR07-TC01)

**Mục đích:** Lấy danh sách booking của customer đang đăng nhập

**Request:**
```http
GET /api/v1/bookings/me
Authorization: Bearer <CUSTOMER_TOKEN>
```

**Luồng thực hiện:**
1. Client gửi request với CUSTOMER token
2. Controller lấy username từ SecurityContext
3. BookingService.getMyBookings() tìm user
4. BookingRepository.findByUserId() lấy tất cả booking
5. Trả về List<BookingResponse>

**Response mong đợi:**
```json
[
    {
        "id": 1,
        "courtId": 1,
        "courtName": "Court 1",
        "timeSlotId": 1,
        "timeSlotLabel": "07:00 - 08:00",
        "bookingDate": "2026-06-20",
        "status": "PENDING",
        "customerName": "Customer User"
    }
]
```

---

#### Bước 2: Manager xem tất cả booking (FR07-TC04)

**Mục đích:** Lấy danh sách tất cả booking trong hệ thống

**Request:**
```http
GET /api/v1/bookings
Authorization: Bearer <MANAGER_TOKEN>
```

**Response mong đợi:**
```json
[
    {
        "id": 1,
        "courtId": 1,
        "courtName": "Court 1",
        "bookingDate": "2026-06-20",
        "status": "PENDING",
        "customerName": "Customer User"
    },
    {
        "id": 2,
        "courtId": 1,
        "courtName": "Court 1",
        "bookingDate": "2026-06-21",
        "status": "CONFIRMED",
        "customerName": "Customer User 2"
    }
]
```

---

#### Bước 3: Lọc theo ngày (FR07-TC05)

**Mục đích:** Lọc booking theo ngày cụ thể

**Request:**
```http
GET /api/v1/bookings?date=2026-06-20
Authorization: Bearer <MANAGER_TOKEN>
```

---

#### Bước 4: Lọc theo status (FR07-TC06)

**Mục đích:** Lọc booking theo trạng thái

**Request:**
```http
GET /api/v1/bookings?status=PENDING
Authorization: Bearer <MANAGER_TOKEN>
```

---

### Cú pháp gọi test (curl)

```bash
# Customer xem lịch của mình
curl http://localhost:8080/api/v1/bookings/me \
  -H "Authorization: Bearer <CUSTOMER_TOKEN>"

# Manager xem tất cả
curl http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer <MANAGER_TOKEN>"

# Lọc theo ngày
curl "http://localhost:8080/api/v1/bookings?date=2026-06-20" \
  -H "Authorization: Bearer <MANAGER_TOKEN>"

# Lọc theo status
curl "http://localhost:8080/api/v1/bookings?status=PENDING" \
  -H "Authorization: Bearer <MANAGER_TOKEN>"
```

---

## FR-08 — Phê duyệt / Từ chối lịch

### Tóm tắt

Chức năng cho phép MANAGER xem danh sách booking và phê duyệt hoặc từ chối các yêu cầu đặt sân đang chờ xử lý.

### Luồng thực hiện

```
Client (MANAGER) → BookingController (PATCH /api/v1/bookings/{id}/status, body BookingStatusUpdateRequest)
                → BookingService.updateBookingStatus()
                → BookingRepository.findById()
                → Kiểm tra status = PENDING
                → Kiểm tra trùng lịch (nếu CONFIRMED)
                → BookingRepository.save()
                → Trả về BookingResponse
```

### Điều kiện để chạy test

1. **Database**
   - Có booking với status = `PENDING` (tạo từ FR06-TC01)
   - MANAGER đã đăng nhập

2. **Tiền điều kiện**
   - Đã có booking PENDING chưa được duyệt

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR08-TC01 | Phê duyệt booking PENDING | `PATCH` body `{ "status": "CONFIRMED" }` | `200 OK`, status = CONFIRMED |
| FR08-TC02 | Từ chối booking PENDING | `PATCH` body `{ "status": "REJECTED" }` | `200 OK`, status = REJECTED |
| FR08-TC03 | Phê duyệt booking không tồn tại | `PATCH /api/v1/bookings/99999/status` | `404 Not Found` |
| FR08-TC04 | Phê duyệt booking đã CONFIRMED | Gọi lại TC01 lần 2 | `400 Bad Request` |
| FR08-TC05 | Phê duyệt booking đã REJECTED | Gọi TC02 trên booking đã reject | `400 Bad Request` |
| FR08-TC06 | Customer tự phê duyệt | Header CUSTOMER | `403 Forbidden` |
| FR08-TC07 | Body thiếu status | `{}` | `400 Bad Request` |
| FR08-TC08 | Status không hợp lệ | `{ "status": "INVALID" }` | `400 Bad Request` |

### Chi tiết từng bước

#### Bước 1: Phê duyệt booking (FR08-TC01)

**Mục đích:** Chuyển booking từ PENDING sang CONFIRMED

**Request:**
```http
PATCH /api/v1/bookings/1/status
Authorization: Bearer <MANAGER_TOKEN>
Content-Type: application/json

{
    "status": "CONFIRMED"
}
```

**Luồng thực hiện:**
1. Client gửi request với MANAGER token
2. BookingService.updateBookingStatus() tìm Booking theo id
3. Kiểm tra booking.getStatus() == PENDING
4. Nếu không phải PENDING, ném BadRequestException
5. Kiểm tra trùng lịch (nếu status = CONFIRMED)
6. Đặt booking.status = CONFIRMED
7. BookingRepository.save() lưu lại

**Response mong đợi:**
```json
{
    "id": 1,
    "courtId": 1,
    "courtName": "Court 1",
    "bookingDate": "2026-06-20",
    "status": "CONFIRMED",
    "customerName": "Customer User"
}
```

**Postman Test:**
```javascript
pm.test('Phê duyệt booking thành công', function() {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.equal('CONFIRMED');
});
```

---

#### Bước 2: Từ chối booking (FR08-TC02)

**Mục đích:** Chuyển booking từ PENDING sang REJECTED

**Request:**
```http
PATCH /api/v1/bookings/2/status
Authorization: Bearer <MANAGER_TOKEN>
Content-Type: application/json

{
    "status": "REJECTED"
}
```

**Response mong đợi:**
```json
{
    "id": 2,
    "status": "REJECTED",
    ...
}
```

---

#### Bước 3: Phê duyệt booking đã CONFIRMED (FR08-TC04)

**Mục đích:** Kiểm tra hệ thống từ chối phê duyệt booking đã được duyệt

**Request:**
```http
PATCH /api/v1/bookings/1/status
Authorization: Bearer <MANAGER_TOKEN>
Content-Type: application/json

{
    "status": "CONFIRMED"
}
```

**Expected Result:** `400 Bad Request`

**Postman Test:**
```javascript
pm.test('Booking đã duyệt không thể duyệt lại', function() {
    pm.response.to.have.status(400);
});
```

---

#### Bước 4: Customer tự phê duyệt (FR08-TC06)

**Mục đích:** Kiểm tra phân quyền - customer không có quyền duyệt

**Request:**
```http
PATCH /api/v1/bookings/1/status
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
    "status": "CONFIRMED"
}
```

**Expected Result:** `403 Forbidden`

---

### Cú pháp gọi test (curl)

```bash
# Phê duyệt booking (thay <BOOKING_ID> bằng ID thật)
curl -X PATCH http://localhost:8080/api/v1/bookings/<BOOKING_ID>/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -d '{"status":"CONFIRMED"}'

# Từ chối booking
curl -X PATCH http://localhost:8080/api/v1/bookings/<BOOKING_ID>/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -d '{"status":"REJECTED"}'
```

---

## FR-09 — Tải lên và lưu trữ nhiều hình ảnh sân cầu

### Tóm tắt

Chức năng cho phép MANAGER upload nhiều ảnh cho một sân cầu, lưu trữ trên Cloudinary và quản lý thông tin ảnh trong database.

### Luồng thực hiện

```
Client (MANAGER) → CourtImageController (POST /api/v1/courts/{courtId}/images, multipart/form-data)
                 → CourtImageService.uploadImages()
                 → CloudStorageService.uploadImage()
                 → Cloudinary API
                 → CourtImageRepository.save()
                 → Trả về List<CourtImageResponse>
```

### Điều kiện để chạy test

1. **Environment Variables**
   ```bash
   CLOUDINARY_CLOUD_NAME=<your_cloud_name>
   CLOUDINARY_API_KEY=<your_api_key>
   CLOUDINARY_API_SECRET=<your_api_secret>
   ```

2. **Database**
   - Court đã tồn tại
   - Bảng `court_images` đã được tạo

3. **Cấu hình** (`application.properties`)
   ```properties
   spring.servlet.multipart.max-file-size=5MB
   spring.servlet.multipart.max-request-size=5MB
   ```

### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR09-TC01 | Upload 1 ảnh hợp lệ | multipart, file jpg < 5MB | `200 OK`, có imageUrl |
| FR09-TC02 | Upload nhiều ảnh | multipart, 3 files | `200 OK`, list 3 imageUrl |
| FR09-TC03 | Upload file > 5MB | file lớn hơn 5MB | `413 Payload Too Large` |
| FR09-TC04 | Upload file không phải ảnh | file .pdf, .exe | `400 Bad Request` |
| FR09-TC05 | Upload cho court không tồn tại | `courtId=99999` | `404 Not Found` |
| FR09-TC06 | Upload không gửi file | body rỗng | `400 Bad Request` |
| FR09-TC07 | Customer upload | Header CUSTOMER | `403 Forbidden` |

### Chi tiết từng bước

#### Bước 1: Upload 1 ảnh hợp lệ (FR09-TC01)

**Mục đích:** Upload ảnh sân lên Cloudinary

**Request:**
```http
POST /api/v1/courts/1/images
Authorization: Bearer <MANAGER_TOKEN>
Content-Type: multipart/form-data

files: [chọn file ảnh]
```

**Luồng thực hiện:**
1. Client gửi request với MANAGER token và file ảnh
2. Controller nhận MultipartFile
3. CourtImageService kiểm tra court tồn tại
4. Validate file: size < 5MB, content-type là ảnh
5. CloudStorageService.uploadImage() upload lên Cloudinary
6. Nhận về secureUrl và publicId
7. CourtImageRepository.save() lưu metadata vào DB

**Response mong đợi:**
```json
[
    {
        "id": 1,
        "courtId": 1,
        "imageUrl": "https://res.cloudinary.com/.../image.jpg",
        "publicId": "badminton/courts/xyz123"
    }
]
```

---

#### Bước 2: Upload nhiều ảnh (FR09-TC02)

**Mục đích:** Upload nhiều ảnh cùng lúc

**Request:**
```http
POST /api/v1/courts/1/images
Authorization: Bearer <MANAGER_TOKEN>
Content-Type: multipart/form-data

files: [file1.jpg, file2.jpg, file3.jpg]
```

**Response mong đợi:**
```json
[
    {"id": 1, "imageUrl": "...", "publicId": "..."},
    {"id": 2, "imageUrl": "...", "publicId": "..."},
    {"id": 3, "imageUrl": "...", "publicId": "..."}
]
```

---

#### Bước 3: Xóa ảnh sân

**Mục đích:** Xóa ảnh khỏi Cloudinary và database

**Request:**
```http
DELETE /api/v1/courts/1/images/1
Authorization: Bearer <MANAGER_TOKEN>
```

**Expected Result:** `204 No Content`

---

### Cú pháp gọi test (curl)

```bash
# Upload 1 ảnh
curl -X POST http://localhost:8080/api/v1/courts/1/images \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -F "files=@/path/to/image.jpg"

# Upload nhiều ảnh
curl -X POST http://localhost:8080/api/v1/courts/1/images \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "files=@/path/to/image3.jpg"

# Xem ảnh sân
curl http://localhost:8080/api/v1/courts/1/images

# Xóa ảnh
curl -X DELETE http://localhost:8080/api/v1/courts/1/images/1 \
  -H "Authorization: Bearer <MANAGER_TOKEN>"
```

---

## FR-10 — Đổi mật khẩu / Quên mật khẩu

### Tóm tắt

Hệ thống cung cấp hai luồng quản lý mật khẩu: đổi mật khẩu khi đã đăng nhập và reset mật khẩu qua token khi quên.

### FR-10A — Đổi mật khẩu

#### Luồng thực hiện

```
Client (đã đăng nhập) → AuthController (POST /api/v1/auth/change-password)
                       → AuthService.changePassword()
                       → UserRepository.findByUsername()
                       → PasswordEncoder.matches()
                       → UserRepository.save()
```

#### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR10-TC01 | Đổi mật khẩu thành công | Header auth, `{ "oldPassword":"Old@123","newPassword":"New@123" }` | `200 OK` |
| FR10-TC02 | Sai mật khẩu cũ | `{ "oldPassword":"wrong","newPassword":"New@123" }` | `400 Bad Request` |
| FR10-TC03 | Mật khẩu mới trùng mật khẩu cũ | `{ "oldPassword":"Old@123","newPassword":"Old@123" }` | `400 Bad Request` |
| FR10-TC04 | Mật khẩu mới yếu | `{ "oldPassword":"Old@123","newPassword":"123" }` | `400 Bad Request` |
| FR10-TC05 | Chưa đăng nhập | Không có header | `401 Unauthorized` |

### Chi tiết từng bước

#### Bước 1: Đổi mật khẩu thành công (FR10-TC01)

**Mục đích:** Thay đổi mật khẩu khi đã đăng nhập

**Request:**
```http
POST /api/v1/auth/change-password
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
    "oldPassword": "Old@123",
    "newPassword": "New@123"
}
```

**Luồng thực hiện:**
1. Client gửi request với token và password cũ/mới
2. AuthService.changePassword() lấy username từ SecurityContext
3. UserRepository.findByUsername() tìm user
4. PasswordEncoder.matches() kiểm tra oldPassword
5. PasswordEncoder.encode() mã hóa newPassword
6. UserRepository.save() lưu password mới

---

### FR-10B — Quên mật khẩu

#### Luồng thực hiện

```
Client → AuthController (POST /api/v1/auth/forgot-password, tham số ForgotPasswordRequest)
      → AuthService.forgotPassword()
      → UserRepository.findByEmail()
      → PasswordResetTokenRepository.save()
      → Trả về ApiResponse
```

#### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR10-TC06 | Gửi yêu cầu với email tồn tại | `{ "email":"user@apex.com" }` | `200 OK`, message gửi reset link |
| FR10-TC07 | Gửi yêu cầu với email không tồn tại | `{ "email":"no@apex.com" }` | `200 OK`, message chung |
| FR10-TC08 | Email rỗng | `{ "email":"" }` | `400 Bad Request` |

#### Chi tiết từng bước

#### Bước 1: Quên mật khẩu - Email tồn tại (FR10-TC06)

**Mục đích:** Yêu cầu reset password qua email

**Request:**
```http
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
    "email": "user@apex.com"
}
```

**Luồng thực hiện:**
1. Client gửi email (không cần token)
2. AuthService.forgotPassword() tìm user theo email
3. Tạo UUID token hết hạn sau 1 giờ
4. Lưu vào bảng password_reset_tokens
5. Trả về message "If the email exists..."

---

### FR-10C — Reset mật khẩu

#### Luồng thực hiện

```
Client → AuthController (POST /api/v1/auth/reset-password, tham số ResetPasswordRequest)
      → AuthService.resetPassword()
      → PasswordResetTokenRepository.findByTokenAndUsedFalse()
      → Kiểm tra token
      → UserRepository.save()
      → Đánh dấu token used=true
```

#### Test Case

| TC-ID | Mô tả | Request | Expected Result |
|-------|-------|---------|-----------------|
| FR10-TC09 | Reset với token hợp lệ | `{ "token":"<valid_token>","newPassword":"Reset@123" }` | `200 OK` |
| FR10-TC10 | Reset với token hết hạn | Token đã hết hạn | `400 Bad Request` |
| FR10-TC11 | Reset với token không hợp lệ | `{ "token":"fake","newPassword":"Reset@123" }` | `400 Bad Request` |
| FR10-TC12 | Reset với token đã dùng | Token có `used=true` | `400 Bad Request` |
| FR10-TC13 | Dùng lại token sau reset | Token vừa dùng ở TC09 | `400 Bad Request` |

### Cú pháp gọi test (curl)

```bash
# Đổi mật khẩu
curl -X POST http://localhost:8080/api/v1/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"oldPassword":"Old@123","newPassword":"New@123"}'

# Quên mật khẩu
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@apex.com"}'

# Reset mật khẩu
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"<reset_token>","newPassword":"Reset@123"}'
```

---

## FR-11 — Ghi log thời gian thực hiện cho tất cả các chức năng (AOP Logging)

### Tóm tắt

Chức năng ghi log tự động cho tất cả method trong controller và service layer bằng Spring AOP, không cần thêm code log thủ công vào từng method.

### Luồng thực hiện

```
HTTP Request → Controller Method (bị intercept bởi LoggingAspect)
            → @Before log [START]
            → Thực thi method thực sự
            → @AfterReturning log [SUCCESS] hoặc @AfterThrowing log [FAILED] + exception message
```

### Điều kiện để chạy test

1. **Cấu hình** (`application.properties`)
   ```properties
   logging.file.name=logs/apexbadminton-audit.log
   logging.level.com.example.badminton.aspect=INFO
   ```

2. **Thư mục logs/**
   - Cần có quyền write để tạo file log

### Test Case

| TC-ID | Mô tả | Kiểm tra |
|-------|-------|----------|
| FR11-TC01 | Mỗi lần login có log | File `logs/apexbadminton-audit.log` có entry LOGIN |
| FR11-TC02 | Mỗi lần logout có log | File có entry LOGOUT |
| FR11-TC03 | Mỗi lần tạo booking có log | File có entry CREATE_BOOKING |
| FR11-TC04 | Mỗi lần phê duyệt/từ chối có log | File có entry UPDATE_BOOKING_STATUS |
| FR11-TC05 | Mỗi lần upload/xóa ảnh có log | File có entry UPLOAD_IMAGE / DELETE_IMAGE |
| FR11-TC06 | Mỗi lần CRUD user có log | File có entry CREATE/UPDATE/DELETE_USER |
| FR11-TC07 | Password không bị lộ trong log | Log hiển thị "***" thay vì password |

### Chi tiết từng bước

#### Bước 1: Kiểm tra log sau khi login

1. Gọi FR01-TC01 (đăng nhập thành công)
2. Mở file `logs/apexbadminton-audit.log`
3. Tìm dòng chứa "AuthController.login" với prefix [START] và [SUCCESS]

**Định dạng log mẫu:**
```
2026-06-12 10:00:00.123 [http-nio-8080-exec-1] INFO  c.e.b.aspect.LoggingAspect - [START] AuthController.login() args=[LoginRequest(username=admin)]
2026-06-12 10:00:00.234 [http-nio-8080-exec-1] INFO  c.e.b.aspect.LoggingAspect - [SUCCESS] AuthController.login()
```

---

#### Bước 2: Kiểm tra log sau khi đặt lịch

1. Gọi FR06-TC01 (đặt lịch thành công)
2. Mở file log
3. Tìm dòng chứa "BookingController.createBooking"

---

#### Bước 3: Kiểm tra log lỗi

1. Gọi FR01-TC02 (đăng nhập sai password)
2. Mở file log
3. Tìm dòng chứa "AuthController.login" với prefix [FAILED]

**Định dạng log lỗi:**
```
2026-06-12 10:00:05.456 [http-nio-8080-exec-2] INFO  c.e.b.aspect.LoggingAspect - [START] AuthController.login() args=[LoginRequest(username=admin)]
2026-06-12 10:00:05.678 [http-nio-8080-exec-2] ERROR c.e.b.aspect.LoggingAspect - [FAILED] AuthController.login() - Bad credentials
```

---

### Cú pháp kiểm tra log

```bash
# Xem log realtime
tail -f logs/apexbadminton-audit.log

# Hoặc mở bằng editor
notepad logs/apexbadminton-audit.log

# Tìm kiếm log của một chức năng cụ thể
grep "LOGIN" logs/apexbadminton-audit.log
grep "CREATE_BOOKING" logs/apexbadminton-audit.log
grep "AuthController" logs/apexbadminton-audit.log
```

---

## Tổng hợp điều kiện chạy toàn bộ hệ thống

### Setup data ban đầu

1. **Đăng ký tài khoản**
   - ADMIN: `admin` / `admin123` (tạo thủ công hoặc seed)
   - MANAGER: `manager` / `manager123`
   - CUSTOMER: `customer` / `customer123`

2. **Tạo dữ liệu mẫu**
   - Tạo 2-3 Court
   - Tạo 5-6 TimeSlot (07:00-08:00, 08:00-09:00, ...)

### Thứ tự test gợi ý

1. **FR-01, FR-02, FR-03**: Authentication (Login, Refresh, Logout)
2. **FR-04**: Đăng ký (Register)
3. **FR-05**: Quản lý User (CRUD, Search, Paginate)
4. **FR-06, FR-07**: Booking (Create, History)
5. **FR-08**: Phê duyệt/Từ chối
6. **FR-09**: Upload ảnh
7. **FR-10**: Đổi/Quên mật khẩu
8. **FR-11**: Kiểm tra file log

---

**Hết tài liệu**
