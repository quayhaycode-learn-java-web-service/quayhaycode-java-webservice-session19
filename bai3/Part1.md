Phần 1 - Phân tích logic
1. Đánh giá tính hiệu quả và hạn chế của cơ chế cũ
   Cơ chế cũ chỉ thu hồi một Refresh Token cụ thể dựa trên chính chuỗi token đó khi người dùng bấm nút đăng xuất. Mô hình này (gọi là Single Session Revocation) bộc lộ hai lỗ hổng nghiệp vụ lớn:

Rủi ro rò rỉ chéo thiết bị: Khi một người dùng đăng nhập trên cả Điện thoại (Mobile) và Máy tính bảng (Tablet). Nếu điện thoại bị mất, người dùng dùng máy tính bảng hoặc máy tính để đổi mật khẩu và bấm "Đăng xuất" với hy vọng kích văng tất cả các phiên độc hại. Ở cơ chế cũ, lệnh này chỉ xóa token của máy tính bảng, còn Refresh Token trên chiếc điện thoại bị mất vẫn hoàn toàn có hiệu lực cho đến khi nó tự hết hạn. Kẻ gian nhặt được điện thoại vẫn có thể lấy Access Token mới để thực hiện giao dịch thanh toán.

Tích tụ rác dữ liệu (Data Bloat): Hệ thống thanh toán có hàng triệu giao dịch mỗi ngày, đồng nghĩa với hàng triệu Refresh Token được sinh ra. Rất nhiều người dùng không bấm "Đăng xuất" mà chỉ đơn giản là gỡ ứng dụng hoặc xóa cache. Nếu không có cơ chế dọn dẹp tự động, các token hết hạn này sẽ tồn tại vĩnh viễn trong Database, làm phình to kích thước bảng, giảm tốc độ truy vấn (SELECT/UPDATE) và gây lãng phí chi phí lưu trữ dữ liệu.

2. Phương án cải tiến chi tiết
   Để giải quyết triệt để, hệ thống cần chuyển dịch sang mô hình Quản lý phiên dựa trên định danh thiết bị (deviceId):

Ràng buộc Token với Thiết bị: Mỗi Refresh Token khi sinh ra phải ký sinh và gắn chặt với một deviceId.

Đa dạng hóa kịch bản Đăng xuất:

Logout cục bộ: Chỉ xóa Refresh Token ứng với cặp (userId, deviceId) hiện tại. Các thiết bị khác giữ nguyên trạng thái đăng nhập.

Logout toàn cục (Kích văng tất cả): Xóa toàn bộ Refresh Token của userId đó, hủy bỏ quyền lực của mọi thiết bị ngay lập tức.

Cơ chế quét rác tự động (Garbage Collection): Xây dựng một tác vụ ngầm định kỳ quét và xóa bỏ các bản ghi có trường expiryDate < NOW().