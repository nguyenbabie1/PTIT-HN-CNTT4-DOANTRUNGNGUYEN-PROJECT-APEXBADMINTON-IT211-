# Postman Test Guide - ApexBadminton API

Base URL:

```text
http://localhost:8080
```

## 0. Chuan Bi Postman

Tao bien trong Postman Environment de thao tac nhanh:

```text
baseUrl = http://localhost:8080
adminToken = accessToken sau khi login admin
managerToken = accessToken sau khi login manager
customerToken = accessToken sau khi login customer
refreshToken = refreshToken sau khi login
bookingId = id booking vua tao
courtId = 1
imageId = id anh san sau khi upload
```

Khi goi API co token:

```text
Authorization -> Type: Bearer Token -> Token: {{customerToken}}
```

Hoac dung token that copy tu response login.

## 1. Tai Khoan Seed Co San

Khi database chua co user, app se tu tao cac tai khoan sau:

```text
admin / admin123
manager / manager123
customer / customer123
```

Quyen:

```text
admin: quan ly user, xem danh sach user, xoa user, xem booking, cap nhat status booking, quan ly anh san, xem bao cao
manager: xem booking, cap nhat status booking, quan ly anh san, xem bao cao
customer: dat san, xem booking cua chinh minh, upload anh hoa don booking cua minh
```

## 2. Dang Ky Tai Khoan Customer

Endpoint:

```http
POST {{baseUrl}}/api/v1/users
```

Postman:

```text
Body -> raw -> JSON
```

Body mau:

```json
{
  "username": "nguyenvana01",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "nguyenvana01@example.com"
}
```

Du lieu test them:

```json
{
  "username": "tranthib02",
  "password": "123456",
  "fullName": "Tran Thi B",
  "email": "tranthib02@example.com"
}
```

```json
{
  "username": "levanc03",
  "password": "123456",
  "fullName": "Le Van C",
  "email": "levanc03@example.com"
}
```

```json
{
  "username": "phamthid04",
  "password": "123456",
  "fullName": "Pham Thi D",
  "email": "phamthid04@example.com"
}
```

Ket qua mong doi:

```text
HTTP 201 Created
role = ROLE_CUSTOMER
enabled = true
```

Luu y:

```text
username va email khong duoc trung.
username toi thieu 3 ky tu.
password toi thieu 6 ky tu.
Tai khoan dang ky public mac dinh co role ROLE_CUSTOMER.
```

## 3. Tao User Co Role Bang Admin

Endpoint nay dung chung `POST /api/v1/users`. Public chi duoc tao customer. Neu muon tao manager/admin, phai dang nhap admin va gui role.

Endpoint:

```http
POST {{baseUrl}}/api/v1/users
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}}
```

Body tao manager:

```json
{
  "username": "manager02",
  "password": "123456",
  "fullName": "Manager 02",
  "email": "manager02@example.com",
  "role": "ROLE_MANAGER"
}
```

Body tao admin:

```json
{
  "username": "admin02",
  "password": "123456",
  "fullName": "Admin 02",
  "email": "admin02@example.com",
  "role": "ROLE_ADMIN"
}
```

Ket qua mong doi:

```text
HTTP 201 Created
```

Test loi:

```text
Khong gan token admin ma gui role ROLE_MANAGER/ROLE_ADMIN -> HTTP 400 Bad Request, message Only admin can assign roles.
```

## 4. Dang Nhap

Endpoint:

```http
POST {{baseUrl}}/api/v1/auth/login
```

Body dang nhap customer:

```json
{
  "username": "customer",
  "password": "customer123"
}
```

Body dang nhap admin:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Body dang nhap manager:

```json
{
  "username": "manager",
  "password": "manager123"
}
```

Response mau:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

Sau khi dang nhap, copy `accessToken` vao bien token tuong ung va copy `refreshToken` vao bien `refreshToken`.

## 5. Lam Moi Token

Endpoint:

```http
POST {{baseUrl}}/api/v1/auth/refresh
```

Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Ket qua mong doi:

```text
HTTP 200 OK
Nhan accessToken moi va refreshToken moi.
```

Test loi:

```text
Dung accessToken thay refreshToken -> HTTP 400 Bad Request, message Invalid refresh token.
Dung refreshToken het han hoac sai -> HTTP 400 Bad Request.
```

## 6. Dang Xuat

Endpoint:

```http
POST {{baseUrl}}/api/v1/auth/logout
```

Authorization:

```text
Type: Bearer Token
Token: {{customerToken}}
```

Body:

```text
De trong.
```

Response mau:

```json
{
  "message": "Logout successful"
}
```

Sau khi dang xuat, access token do bi blacklist va khong dung lai duoc.

Test loi:

```text
Khong gan Authorization -> HTTP 401 Unauthorized hoac 403 Forbidden.
Dung refreshToken de logout -> HTTP 400 Bad Request, message Invalid access token.
```

## 7. Doi Mat Khau

Endpoint:

```http
POST {{baseUrl}}/api/v1/auth/change-password
```

Authorization:

```text
Type: Bearer Token
Token: {{customerToken}}
```

Body:

```json
{
  "oldPassword": "customer123",
  "newPassword": "customer456"
}
```

Ket qua mong doi:

```text
HTTP 200 OK
message = Password changed successfully
```

Test lai:

```text
Dang nhap bang password cu -> HTTP 401 Unauthorized.
Dang nhap bang password moi -> HTTP 200 OK.
```

Luu y:

```text
newPassword toi thieu 6 ky tu.
Sau khi test nen doi lai customer123 neu muon dung tiep data seed quen thuoc.
```

## 8. Quen Mat Khau

Endpoint:

```http
POST {{baseUrl}}/api/v1/auth/forgot-password
```

API nay public, khong can token.

Body:

```json
{
  "email": "customer@apexbadminton.com"
}
```

Ket qua mong doi:

```json
{
  "message": "If the email exists, a password reset link has been sent"
}
```

Luu y khi test hien tai:

```text
Code hien tai chi tao token trong DB, chua gui email va chua tra token trong response.
Muon test reset-password thi lay token trong bang password_reset_tokens.
```

SQL lay token moi nhat:

```sql
SELECT token, user_id, expires_at, used
FROM password_reset_tokens
ORDER BY id DESC
LIMIT 1;
```

## 9. Reset Mat Khau

Endpoint:

```http
POST {{baseUrl}}/api/v1/auth/reset-password
```

API nay public, khong can token.

Body:

```json
{
  "token": "token_lay_tu_bang_password_reset_tokens",
  "newPassword": "customer456"
}
```

Ket qua mong doi:

```text
HTTP 200 OK
message = Password reset successful
```

Test loi:

```text
Token sai, het han hoac da used -> HTTP 400 Bad Request, message Invalid or expired reset token.
newPassword ngan hon 6 ky tu -> HTTP 400 Bad Request.
```

## 10. Lay Danh Sach San

Endpoint:

```http
GET {{baseUrl}}/api/v1/courts
```

API nay khong can token.

Ket qua seed mac dinh thuong co:

```text
courtId: 1
courtId: 2
```

Response mau:

```json
[
  {
    "id": 1,
    "name": "San so 1",
    "active": true,
    "pricePerHour": 150000.00
  },
  {
    "id": 2,
    "name": "San so 2",
    "active": true,
    "pricePerHour": 180000.00
  }
]
```

## 11. Lay Anh Cua San

Endpoint:

```http
GET {{baseUrl}}/api/v1/courts/1/images
```

API nay khong can token.

Response mau:

```json
[
  {
    "id": 1,
    "courtId": 1,
    "imageUrl": "https://res.cloudinary.com/.../image/upload/...jpg",
    "createdAt": "2026-06-11T08:00:00"
  }
]
```

Test loi:

```text
courtId khong ton tai -> HTTP 404 Not Found, message Court not found.
```

## 12. Upload File Chung

Endpoint:

```http
POST {{baseUrl}}/api/v1/files/upload
```

Authorization:

```text
Type: Bearer Token
Token: {{customerToken}} hoac {{managerToken}} hoac {{adminToken}}
```

Postman:

```text
Body -> form-data
Key: file
Type: File
Value: chon file .jpg hoac .png
```

Khong can them `entityType` va `entityId` neu chi muon upload anh len Cloudinary.

Response mau:

```json
{
  "url": "https://res.cloudinary.com/.../image/upload/...jpg",
  "imageId": null,
  "entityType": null,
  "entityId": null
}
```

Dieu kien file:

```text
Chi nhan image/jpeg, image/jpg, image/png.
Dung luong toi da 5MB.
Can cau hinh CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET neu muon upload that.
Neu Cloudinary chua cau hinh dung, API co the tra HTTP 503 Service Unavailable.
```

## 13. Upload Anh Cho San

API nay dung de upload anh va gan vao court.

Endpoint:

```http
POST {{baseUrl}}/api/v1/files/upload
```

Authorization:

```text
Type: Bearer Token
Token: {{managerToken}} hoac {{adminToken}}
```

Postman:

```text
Body -> form-data
Key: file       | Type: File | Value: chon file .jpg hoac .png
Key: entityType | Type: Text | Value: COURT
Key: entityId   | Type: Text | Value: 1
```

Response mau:

```json
{
  "url": "https://res.cloudinary.com/.../image/upload/...jpg",
  "imageId": 1,
  "entityType": "COURT",
  "entityId": 1
}
```

Test sau khi upload:

```http
GET {{baseUrl}}/api/v1/courts/1/images
```

Test loi:

```text
Dung token customer upload entityType COURT -> HTTP 400 Bad Request, message Insufficient permissions for this upload.
entityId la court khong ton tai -> HTTP 404 Not Found.
```

## 14. Xoa Anh Cua San

Endpoint:

```http
DELETE {{baseUrl}}/api/v1/courts/1/images/1
```

Authorization:

```text
Type: Bearer Token
Token: {{managerToken}} hoac {{adminToken}}
```

Trong do:

```text
1 dau tien la courtId.
1 thu hai la imageId.
```

Ket qua mong doi:

```text
HTTP 204 No Content
```

Test loi:

```text
Dung token customer -> HTTP 403 Forbidden.
imageId khong thuoc courtId -> HTTP 404 Not Found, message Court image not found.
```

## 15. Dat San

Endpoint:

```http
POST {{baseUrl}}/api/v1/bookings
```

Yeu cau:

```text
Phai dang nhap bang tai khoan ROLE_CUSTOMER.
Admin hoac manager goi API nay se bi 403 Forbidden.
```

Authorization:

```text
Type: Bearer Token
Token: {{customerToken}}
```

Body mau:

```json
{
  "courtId": 1,
  "bookingDate": "2026-06-15",
  "timeSlotId": 1
}
```

Body test them:

```json
{
  "courtId": 2,
  "bookingDate": "2026-06-16",
  "timeSlotId": 2
}
```

```json
{
  "courtId": 1,
  "bookingDate": "2026-06-17",
  "timeSlotId": 3
}
```

Time slot seed mac dinh:

```text
1 = 08:00 - 10:00
2 = 10:00 - 12:00
3 = 14:00 - 16:00
```

Ket qua mong doi:

```text
HTTP 201 Created
status = PENDING
```

Luu `id` booking trong response vao bien `bookingId`.

Luu y:

```text
bookingDate phai la ngay hien tai hoac tuong lai.
Neu trung courtId + bookingDate + timeSlotId thi API bao Time slot already booked.
Booking moi tao mac dinh la PENDING.
```

## 16. Lay Booking Cua Chinh Minh

Endpoint:

```http
GET {{baseUrl}}/api/v1/bookings/me
```

Authorization:

```text
Type: Bearer Token
Token: {{customerToken}}
```

Ket qua mong doi:

```text
HTTP 200 OK
Tra danh sach booking cua customer dang dang nhap.
```

Test loi:

```text
Dung token admin hoac manager -> HTTP 403 Forbidden.
Khong gan token -> HTTP 401 Unauthorized hoac 403 Forbidden.
```

## 17. Upload Anh Hoa Don Cho Booking

Endpoint:

```http
POST {{baseUrl}}/api/v1/files/upload
```

Authorization:

```text
Type: Bearer Token
Token: {{customerToken}} cua chu booking, hoac {{managerToken}}, hoac {{adminToken}}
```

Postman:

```text
Body -> form-data
Key: file       | Type: File | Value: chon file .jpg hoac .png
Key: entityType | Type: Text | Value: BOOKING
Key: entityId   | Type: Text | Value: {{bookingId}}
```

Response mau:

```json
{
  "url": "https://res.cloudinary.com/.../image/upload/...jpg",
  "imageId": null,
  "entityType": "BOOKING",
  "entityId": 1
}
```

Test sau khi upload:

```http
GET {{baseUrl}}/api/v1/bookings/me
```

Trong response booking se co:

```json
{
  "billImageUrl": "https://res.cloudinary.com/.../image/upload/...jpg"
}
```

Test loi:

```text
Customer upload bill cho booking cua user khac -> HTTP 400 Bad Request, message You can only upload bill for your own booking.
bookingId khong ton tai -> HTTP 404 Not Found.
```

## 18. Test Loi Khi Dat San

### 18.1. Chua Gan Token

Endpoint:

```http
POST {{baseUrl}}/api/v1/bookings
```

Khong gan Authorization.

Ket qua mong doi:

```text
HTTP 401 Unauthorized hoac 403 Forbidden
```

### 18.2. Dung Token Admin Hoac Manager De Dat San

Dang nhap admin hoac manager, sau do goi:

```http
POST {{baseUrl}}/api/v1/bookings
```

Ket qua mong doi:

```text
HTTP 403 Forbidden
```

Ly do:

```text
API dat san chi cho ROLE_CUSTOMER.
```

### 18.3. Dat Trung Lich

Gui cung body dat san 2 lan:

```json
{
  "courtId": 1,
  "bookingDate": "2026-06-15",
  "timeSlotId": 1
}
```

Ket qua lan 2:

```text
HTTP 409 Conflict
Time slot already booked
```

### 18.4. Ngay Dat San O Qua Khu

Body:

```json
{
  "courtId": 1,
  "bookingDate": "2020-01-01",
  "timeSlotId": 1
}
```

Ket qua mong doi:

```text
HTTP 400 Bad Request
```

## 19. Lay Danh Sach Booking Cho Admin/Manager

API nay chi danh cho admin hoac manager.

Endpoint:

```http
GET {{baseUrl}}/api/v1/bookings
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}} hoac {{managerToken}}
```

Loc theo ngay:

```http
GET {{baseUrl}}/api/v1/bookings?date=2026-06-15
```

Loc theo trang thai:

```http
GET {{baseUrl}}/api/v1/bookings?status=PENDING
```

Loc theo ngay va trang thai:

```http
GET {{baseUrl}}/api/v1/bookings?date=2026-06-15&status=PENDING
```

Trang thai hop le de loc:

```text
PENDING
CONFIRMED
REJECTED
CANCELLED
```

Test loi:

```text
Dung status sai, vi du APPROVED -> HTTP 400 Bad Request.
Dung token customer -> HTTP 403 Forbidden.
```

## 20. Cap Nhat Trang Thai Booking

API nay chi danh cho admin hoac manager.

Endpoint:

```http
PATCH {{baseUrl}}/api/v1/bookings/{{bookingId}}/status
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}} hoac {{managerToken}}
```

Body duyet booking:

```json
{
  "status": "CONFIRMED"
}
```

Body tu choi booking:

```json
{
  "status": "REJECTED"
}
```

Ket qua mong doi:

```text
HTTP 200 OK
status = CONFIRMED hoac REJECTED
```

Quy tac trong code:

```text
Chi booking dang PENDING moi duoc cap nhat.
Chi chap nhan status moi la CONFIRMED hoac REJECTED.
Khong chap nhan CANCELLED trong API update status hien tai.
```

Test loi:

```text
Cap nhat booking khong phai PENDING -> HTTP 400 Bad Request, message Only pending bookings can be approved or rejected.
Gui status CANCELLED/PENDING -> HTTP 400 Bad Request, message Status must be CONFIRMED or REJECTED.
bookingId khong ton tai -> HTTP 404 Not Found.
Dung token customer -> HTTP 403 Forbidden.
```

## 21. Lay Booking Theo User

Endpoint:

```http
GET {{baseUrl}}/api/v1/users/{userId}/bookings
```

Vi du:

```http
GET {{baseUrl}}/api/v1/users/3/bookings
```

Quyen:

```text
admin: xem duoc moi user
manager: xem duoc moi user
customer: chi xem duoc booking cua chinh minh
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}} hoac {{managerToken}} hoac {{customerToken}}
```

Test loi:

```text
Customer xem booking cua user khac -> HTTP 403 Forbidden.
```

## 22. Phan Trang Danh Sach User

API nay chi danh cho admin.

Dang nhap admin:

```http
POST {{baseUrl}}/api/v1/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Sau do dung `accessToken` admin de goi:

```http
GET {{baseUrl}}/api/v1/users?page=0&size=2
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}}
```

Test page tiep theo:

```http
GET {{baseUrl}}/api/v1/users?page=1&size=2
```

Test size lon hon:

```http
GET {{baseUrl}}/api/v1/users?page=0&size=5
```

Test sap xep theo username tang dan:

```http
GET {{baseUrl}}/api/v1/users?page=0&size=2&sort=username,asc
```

Test sap xep theo id giam dan:

```http
GET {{baseUrl}}/api/v1/users?page=0&size=2&sort=id,desc
```

Luu y:

```text
page bat dau tu 0, khong phai 1.
Mac dinh neu khong truyen page/size thi size = 10, sort = id,desc.
```

## 23. Tim Kiem User

API nay chi danh cho admin.

Endpoint:

```http
GET {{baseUrl}}/api/v1/users?keyword=nguyen
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}}
```

Test tim theo username:

```http
GET {{baseUrl}}/api/v1/users?keyword=customer
```

Test tim theo fullName:

```http
GET {{baseUrl}}/api/v1/users?keyword=Nguyen
```

Test tim theo email:

```http
GET {{baseUrl}}/api/v1/users?keyword=example.com
```

Test tim kiem ket hop phan trang:

```http
GET {{baseUrl}}/api/v1/users?keyword=nguyen&page=0&size=2
```

Test tim kiem ket hop sap xep:

```http
GET {{baseUrl}}/api/v1/users?keyword=nguyen&page=0&size=2&sort=username,asc
```

API tim kiem theo cac truong:

```text
username
fullName
email
```

## 24. Lay Chi Tiet User

API nay chi danh cho admin.

Endpoint:

```http
GET {{baseUrl}}/api/v1/users/{id}
```

Vi du:

```http
GET {{baseUrl}}/api/v1/users/3
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}}
```

Test loi:

```text
id khong ton tai -> HTTP 404 Not Found.
Dung token manager/customer -> HTTP 403 Forbidden.
```

## 25. Cap Nhat User

API nay chi danh cho admin.

Endpoint:

```http
PUT {{baseUrl}}/api/v1/users/{id}
```

Vi du:

```http
PUT {{baseUrl}}/api/v1/users/4
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}}
```

Body mau:

```json
{
  "fullName": "Nguyen Van A Updated",
  "email": "nguyenvana01.updated@example.com",
  "password": "1234567",
  "role": "ROLE_CUSTOMER",
  "enabled": true
}
```

Co the chi gui truong can update:

```json
{
  "enabled": false
}
```

Role hop le:

```text
ROLE_ADMIN
ROLE_MANAGER
ROLE_CUSTOMER
```

Test loi:

```text
Email trung user khac -> HTTP 409 Conflict.
Email sai dinh dang -> HTTP 400 Bad Request.
password ngan hon 6 ky tu -> HTTP 400 Bad Request.
```

## 26. Xoa User Test Bang API

API nay chi danh cho admin.

B1: lay danh sach user de biet `id`:

```http
GET {{baseUrl}}/api/v1/users
```

B2: xoa user theo id:

```http
DELETE {{baseUrl}}/api/v1/users/4
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}}
```

Thay `4` bang id user can xoa.

Ket qua mong doi:

```text
HTTP 204 No Content
```

Luu y:

```text
Neu user da co booking, co the xoa loi do rang buoc khoa ngoai.
Khi do can xoa booking cua user truoc trong database.
```

SQL xoa user test:

```sql
DELETE FROM bookings WHERE user_id = 4;
DELETE FROM users WHERE id = 4;
```

SQL xoa cac user test dung email example.com:

```sql
DELETE b
FROM bookings b
JOIN users u ON b.user_id = u.id
WHERE u.email LIKE '%@example.com';

DELETE FROM users
WHERE email LIKE '%@example.com'
AND username NOT IN ('admin', 'manager', 'customer');
```

## 27. Bao Cao Doanh Thu

API nay chi danh cho admin hoac manager.

Endpoint:

```http
GET {{baseUrl}}/api/v1/reports/revenues?year=2026&month=6
```

Authorization:

```text
Type: Bearer Token
Token: {{adminToken}} hoac {{managerToken}}
```

Luu y:

```text
Bao cao doanh thu chi tinh booking co status CONFIRMED.
Booking moi tao mac dinh la PENDING, nen co the doanh thu = 0 neu chua co booking CONFIRMED.
Sau khi PATCH booking sang CONFIRMED, goi lai API nay de thay doi doanh thu.
```

Test loi:

```text
Dung token customer -> HTTP 403 Forbidden.
Thieu year hoac month -> HTTP 400 Bad Request.
```

## 28. Checklist Test Nhanh

Chay theo thu tu nay de test day du luong chinh:

```text
1. POST /api/v1/auth/login - dang nhap admin, manager, customer va luu token.
2. POST /api/v1/users - dang ky user customer moi.
3. POST /api/v1/users - admin tao user ROLE_MANAGER hoac ROLE_ADMIN neu can test role.
4. GET /api/v1/courts - lay danh sach san.
5. GET /api/v1/courts/1/images - lay danh sach anh san.
6. POST /api/v1/files/upload - manager/admin upload anh san voi entityType COURT.
7. GET /api/v1/courts/1/images - kiem tra anh san vua upload.
8. POST /api/v1/bookings - customer dat san.
9. GET /api/v1/bookings/me - customer xem booking cua minh.
10. POST /api/v1/files/upload - customer upload anh hoa don voi entityType BOOKING.
11. GET /api/v1/bookings - admin/manager xem tat ca booking.
12. PATCH /api/v1/bookings/{id}/status - admin/manager duyet CONFIRMED hoac tu choi REJECTED.
13. GET /api/v1/reports/revenues?year=2026&month=6 - xem doanh thu.
14. GET /api/v1/users?page=0&size=2 - admin test phan trang user.
15. GET /api/v1/users?keyword=nguyen - admin test tim kiem user.
16. POST /api/v1/auth/change-password - user doi mat khau.
17. POST /api/v1/auth/forgot-password - tao reset token.
18. POST /api/v1/auth/reset-password - reset mat khau bang token trong DB.
19. DELETE /api/v1/courts/{courtId}/images/{imageId} - manager/admin xoa anh san test.
20. DELETE /api/v1/users/{id} - admin xoa user test neu can.
```

## 29. Checklist Phan Quyen

```text
POST /api/v1/users public: tao customer neu khong gui role hoac role ROLE_CUSTOMER.
POST /api/v1/users admin: tao duoc ROLE_ADMIN/ROLE_MANAGER/ROLE_CUSTOMER.
POST /api/v1/auth/login public.
POST /api/v1/auth/refresh public.
POST /api/v1/auth/forgot-password public.
POST /api/v1/auth/reset-password public.
POST /api/v1/auth/logout can authenticated.
POST /api/v1/auth/change-password can authenticated.
GET /api/v1/courts public.
GET /api/v1/courts/{courtId}/images public.
POST /api/v1/bookings chi ROLE_CUSTOMER.
GET /api/v1/bookings/me chi ROLE_CUSTOMER.
GET /api/v1/bookings chi ROLE_ADMIN hoac ROLE_MANAGER.
PATCH /api/v1/bookings/{id}/status chi ROLE_ADMIN hoac ROLE_MANAGER.
GET /api/v1/users chi ROLE_ADMIN.
GET /api/v1/users/{id} chi ROLE_ADMIN.
PUT /api/v1/users/{id} chi ROLE_ADMIN.
DELETE /api/v1/users/{id} chi ROLE_ADMIN.
GET /api/v1/users/{userId}/bookings admin/manager xem moi user, customer chi xem chinh minh.
POST /api/v1/files/upload authenticated; nhung entityType COURT chi admin/manager, entityType BOOKING chu booking hoac admin/manager.
DELETE /api/v1/courts/{courtId}/images/{imageId} chi ROLE_ADMIN hoac ROLE_MANAGER.
GET /api/v1/reports/revenues chi ROLE_ADMIN hoac ROLE_MANAGER.
```

## 30. Loi Thuong Gap

403 Forbidden:

```text
Sai role hoac chua gan token.
Dat san phai dung token customer.
Quan ly user phai dung token admin.
Bao cao, xem booking, duyet booking dung token admin hoac manager.
Xoa/upload anh san dung token admin hoac manager.
```

401 Unauthorized:

```text
Token thieu, token sai, token het han hoac da logout.
Sai username/password khi login.
```

409 Conflict:

```text
Username/email da ton tai, lich san da bi dat, account bi khoa, hoac san khong active.
```

400 Bad Request:

```text
Body JSON sai, thieu field bat buoc, email sai dinh dang, password ngan hon 6 ky tu, bookingDate o qua khu, status booking khong hop le, upload thieu file, file sai dinh dang, file lon hon 5MB.
```

404 Not Found:

```text
Khong tim thay user, booking, court, time slot hoac court image.
```

503 Service Unavailable:

```text
Loi upload Cloudinary hoac chua cau hinh Cloudinary dung.
```
