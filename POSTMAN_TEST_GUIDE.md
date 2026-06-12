# ApexBadminton API - Hướng Dẫn Test Postman

## Môi Trường

```
Base URL: http://localhost:8080/api/v1
Content-Type: application/json
```

## Tài Khoản Seed Data (Demo)

| Role | Username | Password |
|------|----------|----------|
| ADMIN | `admin` | `admin123` |
| MANAGER | `manager` | `manager123` |
| CUSTOMER | `customer` | `customer123` |

---

## Bước 0 — Biến Môi Trường Postman

Tạo Collection variable `{{baseUrl}}` = `http://localhost:8080/api/v1`

Tạo Collection variable `{{accessToken}}` (sẽ được set tự động sau login)

---

## PHẦN 1 — XÁC THỰC & AUTHENTICATION

### 1.1 Đăng nhập (Admin) → Lấy Access Token

```
POST {{baseUrl}}/auth/login
```

**Body (raw JSON):**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Kiểm tra:**
- Status: `200 OK`
- Response chứa `accessToken`, `refreshToken`
- Copy giá trị `accessToken` → paste vào biến `{{accessToken}}`

---

### 1.2 Đăng nhập (Manager)

```
POST {{baseUrl}}/auth/login
```

**Body:**
```json
{
    "username": "manager",
    "password": "manager123"
}
```

---

### 1.3 Đăng nhập (Customer)

```
POST {{baseUrl}}/auth/login
```

**Body:**
```json
{
    "username": "customer",
    "password": "customer123"
}
```

---

### 1.4 Refresh Token

```
POST {{baseUrl}}/auth/refresh
```

**Body:**
```json
{
    "refreshToken": "{{refreshToken}}"
}
```

> Lấy `refreshToken` từ response của Bước 1.1

---

### 1.5 Đăng xuất (Logout)

```
POST {{baseUrl}}/auth/logout
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `200 OK`
- Message: `"Logout successful"`

---

### 1.6 Đổi mật khẩu

```
POST {{baseUrl}}/auth/change-password
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "oldPassword": "admin123",
    "newPassword": "Admin@1234"
}
```

> Đổi xong → login lại với mật khẩu mới để tiếp tục test

---

### 1.7 Quên mật khẩu

```
POST {{baseUrl}}/auth/forgot-password
```

**Body:**
```json
{
    "email": "admin@apexbadminton.com"
}
```

**Kiểm tra:**
- Status: `200 OK`
- Message: `"If the email exists, a password reset link has been sent"`
- Kiểm tra bảng `password_reset_tokens` trong DB để lấy token test

---

### 1.8 Reset mật khẩu

```
POST {{baseUrl}}/auth/reset-password
```

**Body:**
```json
{
    "token": "<lấy từ bảng password_reset_tokens>",
    "newPassword": "admin123"
}
```

---

## PHẦN 2 — QUẢN LÝ NGƯỜI DÙNG

### 2.1 Đăng ký tài khoản Customer (Public)

```
POST {{baseUrl}}/users
```

**Body:**
```json
{
    "username": "nguyenvana",
    "password": "test123456",
    "fullName": "Nguyen Van A",
    "email": "nguyenvana@example.com"
}
```

**Kiểm tra:**
- Status: `201 Created`
- Response chứa thông tin user (không có password)

---

### 2.2 Tạo tài khoản Manager (Admin only)

```
POST {{baseUrl}}/users
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "username": "manager2",
    "password": "manager2123",
    "fullName": "Quan Ly Thu Hai",
    "email": "manager2@apexbadminton.com",
    "role": "ROLE_MANAGER"
}
```

**Kiểm tra:**
- Status: `201 Created`
- role = `"ROLE_MANAGER"`

---

### 2.3 Tạo tài khoản Customer (Admin only)

```
POST {{baseUrl}}/users
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "username": "customer2",
    "password": "customer2123",
    "fullName": "Khach Hang Thu Hai",
    "email": "customer2@apexbadminton.com",
    "role": "ROLE_CUSTOMER"
}
```

---

### 2.4 Xem danh sách người dùng (Admin only)

```
GET {{baseUrl}}/users
Authorization: Bearer {{accessToken}}
```

**Query params (tùy chọn):**
```
?keyword=admin
?page=0&size=5&sort=id,desc
```

**Kiểm tra:**
- Status: `200 OK`
- Response là Page object chứa `content[]`, `totalPages`, `totalElements`

---

### 2.5 Xem chi tiết người dùng (Admin only)

```
GET {{baseUrl}}/users/1
Authorization: Bearer {{accessToken}}
```

---

### 2.6 Cập nhật người dùng (Admin only)

```
PUT {{baseUrl}}/users/3
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "fullName": "Nguyen Van A - Updated",
    "email": "nguyenvana.updated@example.com",
    "enabled": true
}
```

---

### 2.7 Xóa người dùng (Admin only)

```
DELETE {{baseUrl}}/users/4
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `204 No Content`

---

## PHẦN 3 — QUẢN LÝ SÂN (COURTS)

### 3.1 Xem danh sách sân (Public)

```
GET {{baseUrl}}/courts
```

**Kiểm tra:**
- Status: `200 OK`
- Response: `[{id, name, active, pricePerHour, images[]}]`

---

### 3.2 Xem chi tiết 1 sân (Public)

```
GET {{baseUrl}}/courts/1
```

---

### 3.3 Tra cứu sân trống theo ngày (Public)

```
GET {{baseUrl}}/courts/available?date=2026-06-20
```

**Kiểm tra:**
- Chỉ trả về sân `active = true` và chưa có booking `PENDING/CONFIRMED` vào ngày đó

---

### 3.4 Tạo sân mới (Manager/Admin)

```
POST {{baseUrl}}/courts
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "name": "Sân số 3",
    "pricePerHour": 200000,
    "active": true
}
```

**Kiểm tra:**
- Status: `201 Created`
- Response chứa `id` của sân mới → copy `id` để test các bước tiếp theo

---

### 3.5 Cập nhật sân (Manager/Admin)

```
PUT {{baseUrl}}/courts/3
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "name": "Sân số 3 - VIP",
    "pricePerHour": 250000,
    "active": true
}
```

---

### 3.6 Vô hiệu hóa sân (Manager/Admin)

```
PUT {{baseUrl}}/courts/3
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "active": false
}
```

---

### 3.7 Xóa sân (Manager/Admin)

```
DELETE {{baseUrl}}/courts/3
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `204 No Content`

---

## PHẦN 4 — QUẢN LÝ KHUNG GIỜ (TIME SLOTS)

### 4.1 Xem danh sách khung giờ (Public)

```
GET {{baseUrl}}/time-slots
```

---

### 4.2 Xem chi tiết khung giờ (Public)

```
GET {{baseUrl}}/time-slots/1
```

---

### 4.3 Tạo khung giờ mới (Manager/Admin)

```
POST {{baseUrl}}/time-slots
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "label": "16:00 - 18:00",
    "startTime": "16:00:00",
    "endTime": "18:00:00"
}
```

**Kiểm tra:**
- Status: `201 Created`
- Copy `id` để test booking

---

### 4.4 Cập nhật khung giờ (Manager/Admin)

```
PUT {{baseUrl}}/time-slots/4
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "label": "16:00 - 18:00",
    "startTime": "16:00:00",
    "endTime": "18:30:00"
}
```

---

### 4.5 Xóa khung giờ (Manager/Admin)

```
DELETE {{baseUrl}}/time-slots/4
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `204 No Content`

---

## PHẦN 5 — ĐẶT SÂN (BOOKING)

### 5.1 Đặt sân (Customer) — Lấy token Customer

**Trước tiên:** Login customer và cập nhật `{{accessToken}}`

```
POST {{baseUrl}}/auth/login
```

```json
{
    "username": "customer",
    "password": "customer123"
}
```

---

### 5.2 Tạo booking (Customer)

```
POST {{baseUrl}}/bookings
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "courtId": 1,
    "bookingDate": "2026-06-20",
    "timeSlotId": 1
}
```

**Kiểm tra:**
- Status: `201 Created`
- `status`: `"PENDING"`
- Copy `id` của booking để test phê duyệt

---

### 5.3 Đặt sân trùng khung giờ (Kiểm tra lỗi)

```
POST {{baseUrl}}/bookings
Authorization: Bearer {{accessToken}}
```

**Body (trùng với booking ở 5.2):**
```json
{
    "courtId": 1,
    "bookingDate": "2026-06-20",
    "timeSlotId": 1
}
```

**Kiểm tra:**
- Status: `409 Conflict`
- Message: `"Time slot already booked"`

---

### 5.4 Xem lịch sử đặt sân của mình (Customer)

```
GET {{baseUrl}}/bookings/me
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `200 OK`
- Danh sách các booking của customer đang login

---

### 5.5 Xem tất cả booking (Manager/Admin) — Lấy token Manager

**Login manager → cập nhật `{{accessToken}}`**

```
POST {{baseUrl}}/auth/login
```

```json
{
    "username": "manager",
    "password": "manager123"
}
```

---

### 5.6 Danh sách tất cả booking (Manager/Admin)

```
GET {{baseUrl}}/bookings
Authorization: Bearer {{accessToken}}
```

**Query params (tùy chọn):**
```
?date=2026-06-20
?status=PENDING
?date=2026-06-20&status=PENDING
```

---

### 5.7 Phê duyệt booking (CONFIRMED) (Manager/Admin)

```
PATCH {{baseUrl}}/bookings/1/status
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "status": "CONFIRMED"
}
```

**Kiểm tra:**
- Status: `200 OK`
- `status`: `"CONFIRMED"`

---

### 5.8 Từ chối booking (REJECTED) (Manager/Admin)

```
PATCH {{baseUrl}}/bookings/2/status
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "status": "REJECTED"
}
```

**Kiểm tra:**
- Status: `200 OK`
- `status`: `"REJECTED"`

---

### 5.9 Cập nhật booking đã xác nhận (Kiểm tra lỗi)

```
PATCH {{baseUrl}}/bookings/1/status
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "status": "REJECTED"
}
```

**Kiểm tra:**
- Status: `400 Bad Request`
- Message: `"Only pending bookings can be approved or rejected"`

---

## PHẦN 6 — UPLOAD HÌNH ẢNH SÂN (CLOUDINARY)

### 6.1 Upload ảnh sân (Manager/Admin)

```
POST {{baseUrl}}/files/upload
Authorization: Bearer {{accessToken}}
Content-Type: multipart/form-data
```

**Form Data:**
| Key | Value |
|-----|-------|
| `file` | Chọn file ảnh (PNG/JPG, < 5MB) |
| `entityType` | `COURT` |
| `entityId` | `1` |

**Kiểm tra:**
- Status: `200 OK`
- Response: `{url, imageId, entityType, entityId}`

---

### 6.2 Xem danh sách ảnh của sân (Public)

```
GET {{baseUrl}}/courts/1/images
```

---

### 6.3 Xóa ảnh sân (Manager/Admin)

```
DELETE {{baseUrl}}/courts/1/images/1
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `204 No Content`

---

## PHẦN 7 — UPLOAD ẢNH BILL (HÓA ĐƠN)

### 7.1 Upload ảnh bill cho booking (Customer)

```
POST {{baseUrl}}/files/upload
Authorization: Bearer {{accessToken}}
Content-Type: multipart/form-data
```

**Form Data:**
| Key | Value |
|-----|-------|
| `file` | Chọn file ảnh bill (PNG/JPG, < 5MB) |
| `entityType` | `BOOKING` |
| `entityId` | `1` |

---

### 7.2 Xác nhận booking đã có bill (Manager/Admin)

```
PATCH {{baseUrl}}/bookings/1/status
Authorization: Bearer {{accessToken}}
```

**Body:**
```json
{
    "status": "CONFIRMED"
}
```

---

## PHẦN 8 — BÁO CÁO DOANH THU

### 8.1 Báo cáo doanh thu tháng (Manager/Admin)

```
GET {{baseUrl}}/reports/revenues?year=2026&month=6
Authorization: Bearer {{accessToken}}
```

**Kiểm tra:**
- Status: `200 OK`
- Response chứa danh sách booking `CONFIRMED` trong tháng + tổng doanh thu

---

## PHẦN 9 — TEST LỖI

### 9.1 Đăng nhập sai mật khẩu

```
POST {{baseUrl}}/auth/login
```

```json
{
    "username": "admin",
    "password": "wrongpassword"
}
```

**Kiểm tra:**
- Status: `401 Unauthorized`
- Message: `"Invalid username or password"`

---

### 9.2 Gọi API cần quyền mà không có token

```
GET {{baseUrl}}/users
```

**Kiểm tra:**
- Status: `401 Unauthorized` hoặc `403 Forbidden`

---

### 9.3 Customer cố tạo sân (Không đủ quyền)

**Login customer → cập nhật `{{accessToken}}`**

```
POST {{baseUrl}}/courts
Authorization: Bearer {{accessToken}}
```

```json
{
    "name": "Sân lậu",
    "pricePerHour": 100000,
    "active": true
}
```

**Kiểm tra:**
- Status: `403 Forbidden`
- Message: `"Access denied"`

---

### 9.4 Upload file quá 5MB

```
POST {{baseUrl}}/files/upload
Authorization: Bearer {{accessToken}}
Content-Type: multipart/form-data
```

**Form Data:**
| Key | Value |
|-----|-------|
| `file` | Chọn file > 5MB |
| `entityType` | `COURT` |
| `entityId` | `1` |

**Kiểm tra:**
- Status: `400 Bad Request`
- Message: `"File size must be less than 5MB"`

---

### 9.5 Upload file không đúng định dạng

```
POST {{baseUrl}}/files/upload
Authorization: Bearer {{accessToken}}
Content-Type: multipart/form-data
```

**Form Data:**
| Key | Value |
|-----|-------|
| `file` | Chọn file `.pdf` hoặc `.gif` |
| `entityType` | `COURT` |
| `entityId` | `1` |

**Kiểm tra:**
- Status: `400 Bad Request`
- Message: `"Only PNG and JPG images are allowed"`

---

### 9.6 Tạo booking với sân không tồn tại

```
POST {{baseUrl}}/bookings
Authorization: Bearer {{accessToken}}
```

```json
{
    "courtId": 9999,
    "bookingDate": "2026-06-25",
    "timeSlotId": 1
}
```

**Kiểm tra:**
- Status: `404 Not Found`
- Message: `"Court not found"`

---

### 9.7 Tạo booking với ngày trong quá khứ

```
POST {{baseUrl}}/bookings
Authorization: Bearer {{accessToken}}
```

```json
{
    "courtId": 1,
    "bookingDate": "2025-01-01",
    "timeSlotId": 1
}
```

**Kiểm tra:**
- Status: `400 Bad Request`

---

### 9.8 Khung giờ kết thúc trước bắt đầu

```
POST {{baseUrl}}/time-slots
Authorization: Bearer {{accessToken}}
```

```json
{
    "label": "Ca sai",
    "startTime": "18:00:00",
    "endTime": "10:00:00"
}
```

**Kiểm tra:**
- Status: `400 Bad Request`
- Message: `"End time must be after start time"`

---

## PHẦN 10 — REFRESH TOKEN WORKFLOW

**Mục đích:** Test luồng hết hạn access token → dùng refresh token lấy token mới

### 10.1 Sau khi access token hết hạn (30 phút)

```
POST {{baseUrl}}/auth/refresh
```

```json
{
    "refreshToken": "{{refreshToken}}"
}
```

**Kiểm tra:**
- Status: `200 OK`
- Nhận `accessToken` mới và `refreshToken` mới
- Cập nhật biến `{{accessToken}}` = accessToken mới

---

## Tổng Kết Checklist

| # | API | Method | Pass |
|---|-----|--------|------|
| 1 | Login | POST /auth/login | ☐ |
| 2 | Refresh Token | POST /auth/refresh | ☐ |
| 3 | Logout | POST /auth/logout | ☐ |
| 4 | Change Password | POST /auth/change-password | ☐ |
| 5 | Forgot Password | POST /auth/forgot-password | ☐ |
| 6 | Reset Password | POST /auth/reset-password | ☐ |
| 7 | Register User | POST /users | ☐ |
| 8 | List Users (paging) | GET /users | ☐ |
| 9 | Get User by ID | GET /users/{id} | ☐ |
| 10 | Update User | PUT /users/{id} | ☐ |
| 11 | Delete User | DELETE /users/{id} | ☐ |
| 12 | List Courts | GET /courts | ☐ |
| 13 | Get Court by ID | GET /courts/{id} | ☐ |
| 14 | Available Courts | GET /courts/available | ☐ |
| 15 | Create Court | POST /courts | ☐ |
| 16 | Update Court | PUT /courts/{id} | ☐ |
| 17 | Delete Court | DELETE /courts/{id} | ☐ |
| 18 | List Time Slots | GET /time-slots | ☐ |
| 19 | Get Time Slot by ID | GET /time-slots/{id} | ☐ |
| 20 | Create Time Slot | POST /time-slots | ☐ |
| 21 | Update Time Slot | PUT /time-slots/{id} | ☐ |
| 22 | Delete Time Slot | DELETE /time-slots/{id} | ☐ |
| 23 | Create Booking | POST /bookings | ☐ |
| 24 | My Bookings | GET /bookings/me | ☐ |
| 25 | List All Bookings | GET /bookings | ☐ |
| 26 | Approve Booking | PATCH /bookings/{id}/status CONFIRMED | ☐ |
| 27 | Reject Booking | PATCH /bookings/{id}/status REJECTED | ☐ |
| 28 | Upload Court Image | POST /files/upload COURT | ☐ |
| 29 | List Court Images | GET /courts/{id}/images | ☐ |
| 30 | Delete Court Image | DELETE /courts/{id}/images/{imageId} | ☐ |
| 31 | Upload Bill Image | POST /files/upload BOOKING | ☐ |
| 32 | Revenue Report | GET /reports/revenues | ☐ |
| 33 | Booking Conflict | POST /bookings (trùng slot) | ☐ |
| 34 | Unauthorized Access | GET /users (no token) | ☐ |
| 35 | Forbidden | POST /courts (customer) | ☐ |
| 36 | File too large | POST /files/upload (>5MB) | ☐ |
| 37 | Invalid file type | POST /files/upload (.pdf) | ☐ |
| 38 | Invalid booking date | POST /bookings (past) | ☐ |
| 39 | Invalid time slot range | POST /time-slots (end before start) | ☐ |
