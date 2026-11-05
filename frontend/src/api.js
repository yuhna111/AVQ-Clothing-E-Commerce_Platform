// Gọi REST API /api/v1 của backend; tự gắn JWT và chuyển lỗi nghiệp vụ thành Error có code, message.
export const money = (v) => (v == null ? "" : Number(v).toLocaleString("vi-VN") + " đ");
export const STATUS = {
  CHO_THANH_TOAN: "Chờ thanh toán", CHO_XAC_NHAN: "Chờ xác nhận", DANG_GIAO: "Đang giao", DA_GIAO: "Đã giao", DA_HUY: "Đã hủy",
};
export const fmtTime = (s) => (s ? new Date(s).toLocaleString("vi-VN") : "");

export function getSession() {
  try { return JSON.parse(localStorage.getItem("avq_session")) || null; } catch { return null; }
}

export async function api(path, { method = "GET", body, params } = {}) {
  const qs = params ? "?" + new URLSearchParams(Object.entries(params).filter(([, v]) => v !== "" && v != null)) : "";
  const s = getSession();
  const res = await fetch("/api/v1" + path + qs, {
    method,
    headers: { "Content-Type": "application/json", ...(s ? { Authorization: "Bearer " + s.token } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  return readResponse(res);
}

// UC15: nhân viên tải ảnh sản phẩm từ thiết bị (multipart) → { url: "/uploads/..." }
export async function uploadImage(file) {
  const s = getSession();
  const form = new FormData();
  form.append("file", file);
  const res = await fetch("/api/v1/admin/uploads/images", {
    method: "POST",
    headers: s ? { Authorization: "Bearer " + s.token } : {},
    body: form,
  });
  return readResponse(res);
}

async function readResponse(res) {
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (!res.ok) {
    const err = new Error((data && data.message) || "Có lỗi xảy ra (" + res.status + ")");
    err.code = data && data.code; err.status = res.status;
    throw err;
  }
  return data;
}
