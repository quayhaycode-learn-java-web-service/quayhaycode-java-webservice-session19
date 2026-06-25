Phần 1 - Phân tích logic
Hệ thống giao dịch chứng khoán đòi hỏi tính toàn vẹn và bảo mật ở mức tối cao. Phân tích đoạn code InsecureTokenService chỉ ra hai lỗ hổng chí mạng:

1. Lỗ hổng HARDCODED_SECRET_KEY
   Nguyên nhân: Khóa bí mật ký JWT ("ThisIsAVerySecretKeyButItsHardcodedAndTooShort") đang được lưu trực tiếp dưới dạng một hằng số chuỗi (String literal) trong mã nguồn.

Kịch bản khai thác: * Lộ mã nguồn: Nếu kho lưu trữ code (GitHub, GitLab) bị cấu hình sai chế độ công khai, hoặc máy tính của lập trình viên bị mã độc tấn công, kẻ gian sẽ có toàn bộ mã nguồn cùng Secret Key.

Kỹ thuật Reverse Engineering: Đối với các ứng dụng Java, kẻ tấn công có thể dễ dàng dịch ngược file .class hoặc file .jar thành mã nguồn ban đầu thông qua các công cụ Decompiler (như JD-GUI, CFR) để lấy chuỗi Key này.

Hậu quả: Khi có được Secret Key, kẻ tấn công nắm quyền tối cao đối với cơ chế xác thực. Họ tự đóng vai trò là "Cơ quan cấp phát Token" (Issuer), tự ký ra các JWT giả mạo (Forged Token) với nội dung Claims tùy ý (ví dụ: gán bản thân thành tài khoản admin hoặc tài khoản của một nhà đầu tư VIP). Hệ thống hoàn toàn bị qua mặt vì chữ ký trên token giả mạo khớp 100% với Secret Key bị lộ. Kẻ tấn công có thể đặt lệnh mua bán khống, rút tiền hoặc đánh cắp toàn bộ thông tin danh mục đầu tư.

2. Lỗ hổng ACCESS_TOKEN_EXPIRATION_DAYS = 30
   Nguyên nhân: Đặt thời gian sống của Access Token lên tới 30 ngày.

Kịch bản khai thác: Access Token lưu ở phía Client (Local Storage, Session Storage hoặc Cookies) rất dễ bị đánh cắp thông qua các cuộc tấn công Cross-Site Scripting (XSS), Man-in-the-middle (MITM) tại các mạng Wi-Fi công cộng, hoặc bị log lại ở các tầng Proxy/Gateway trung gian.

Hậu quả: Trong mô hình JWT thuần túy (Stateless), một khi token đã được phát hành, phía Backend không thể chủ động thu hồi (Revoke) nếu không áp dụng các cơ chế Blacklist phức tạp. Do đó, một khi Access Token có thời hạn 30 ngày bị lộ, kẻ tấn công có quyền truy cập hợp pháp vào tài khoản nạn nhân suốt 720 giờ liên tục mà không cần biết mật khẩu, tạo ra một lỗ hổng thời gian (Attack Window) quá lớn cho một hệ thống tài chính.