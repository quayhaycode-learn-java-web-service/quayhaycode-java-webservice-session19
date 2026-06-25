Phần 1 - Phân tích logic
Dựa trên đoạn mã nguồn của lớp TokenService, nguyên nhân gốc rễ khiến người dùng phải liên tục đăng nhập lại sau mỗi 30 giây nằm ở dòng cấu hình thời gian hết hạn:

Java
private final long ACCESS_TOKEN_EXPIRATION_MS = TimeUnit.SECONDS.toMillis(30); // 30 giây
Phân tích chi tiết:
Sai lệch đơn vị cấu hình: Biến ACCESS_TOKEN_EXPIRATION_MS đang sử dụng hàm TimeUnit.SECONDS.toMillis(30). Hàm này đổi 30 giây thành 30,000 mili giây.

Cơ chế JWT Expiration: Khi phương thức generateAccessToken được gọi, expiryDate được tính bằng thời gian hiện tại cộng thêm 30,000 mili giây (30 giây). Thư viện JWT sẽ ghi nhận mốc thời gian này vào claim exp.

Hậu quả nghiệp vụ: Sau đúng 30 giây kể từ khi được cấp, mọi yêu cầu từ client gửi kèm Access Token này sẽ bị phương thức validateToken từ chối (trả về false) do token đã quá hạn. Do hệ thống chưa áp dụng cơ chế tự động làm mới (Refresh Token), client bắt buộc phải điều hướng người dùng quay lại màn hình đăng nhập, phá vỡ trải nghiệm liền mạch của ứng dụng e-commerce.