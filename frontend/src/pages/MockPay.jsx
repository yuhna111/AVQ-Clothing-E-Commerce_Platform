import React, { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api, money } from "../api.js";

// Trang giả lập cổng thanh toán (thay VNPay sandbox khi demo). Kết quả được ký HMAC và xử lý bởi đúng PaymentService.handleCallback().
export default function MockPay() {
  const [p] = useSearchParams();
  const nav = useNavigate();
  const [res, setRes] = useState(null);
  const code = p.get("order");
  async function pay(success) {
    const r = await api(`/payments/mock/${code}`, { method: "POST", params: { success } });
    setRes(r);
  }
  return (
    <div className="center-box panel">
      <h1>Cổng thanh toán (giả lập)</h1>
      <p>Đơn hàng: <b>{code}</b></p>
      <p>Số tiền: <span className="price">{money(p.get("amount"))}</span></p>
      {!res ? (
        <div className="inline">
          <button className="btn" onClick={() => pay(true)}>Thanh toán thành công</button>
          <button className="btn red" onClick={() => pay(false)}>Thanh toán thất bại</button>
        </div>
      ) : (
        <>
          <p>Kết quả IPN: mã <b>{res.rspCode}</b> ({res.message}). Trạng thái đơn: <b>{res.orderStatus}</b></p>
          <button className="btn" onClick={() => nav("/don-hang/" + code)}>Xem đơn hàng</button>
        </>
      )}
    </div>
  );
}
