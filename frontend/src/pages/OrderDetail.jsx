import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, money, STATUS, fmtTime } from "../api.js";
import { useApp } from "../state.jsx";

export function OrderView({ o }) {
  return (
    <>
      <div className="panel">
        <div className="inline" style={{ justifyContent: "space-between" }}>
          <h2 style={{ margin: 0 }}>Đơn {o.orderCode}</h2><span className={"status " + o.status}>{STATUS[o.status]}</span>
        </div>
        <p className="muted">Đặt lúc {fmtTime(o.orderDate)} · {o.paymentMethod === "COD" ? "Thanh toán khi nhận hàng" : "Thanh toán trực tuyến"}
          {o.expiresAt && " · giữ hàng đến " + fmtTime(o.expiresAt)}{o.trackingCode && " · Mã vận đơn: " + o.trackingCode}</p>
        <p>Người nhận: <b>{o.receiverName}</b> · {o.receiverPhone}<br />{o.shippingAddress}</p>
        <table>
          <thead><tr><th>Sản phẩm</th><th>Phân loại</th><th>Đơn giá</th><th>SL</th><th>Thành tiền</th></tr></thead>
          <tbody>{o.items.map((i, k) => <tr key={k}><td>{i.productName}</td><td>{i.size} / {i.color}</td><td>{money(i.unitPrice)}</td><td>{i.quantity}</td><td>{money(i.lineTotal)}</td></tr>)}</tbody>
        </table>
        <div className="summary" style={{ maxWidth: 320, marginLeft: "auto", marginTop: 10 }}>
          <div className="line"><span>Tạm tính</span><span>{money(o.subtotal)}</span></div>
          <div className="line"><span>Phí giao</span><span>{money(o.shippingFee)}</span></div>
          <div className="line"><b>Tổng</b><span className="total">{money(o.total)}</span></div>
        </div>
      </div>
      <div className="panel" style={{ marginTop: 16 }}>
        <h2>Lịch sử trạng thái</h2>
        <ul className="timeline">{o.history.map((h, k) => (
          <li key={k}>{fmtTime(h.changedAt)} — {h.fromStatus ? STATUS[h.fromStatus] + " → " : ""}<b>{STATUS[h.toStatus]}</b>
            {h.note && <> · <i>{h.note}</i></>}{h.staffName && <span className="muted"> (bởi {h.staffName})</span>}</li>))}
        </ul>
      </div>
    </>
  );
}

// UC11 chi tiết đơn, UC12 hủy đơn (BR13)
export default function OrderDetail() {
  const { code } = useParams();
  const { notify } = useApp();
  const [o, setO] = useState(null);
  useEffect(() => { api("/orders/" + code).then(setO).catch((e) => notify(e.message, "err")); }, [code]);
  if (!o) return <p className="muted">Đang tải...</p>;
  async function cancel() {
    const reason = prompt("Lý do hủy đơn?", "Đổi ý");
    if (reason === null) return;
    try { setO(await api(`/orders/${code}/cancel`, { method: "POST", body: { reason } })); notify("Đã hủy đơn, tồn kho đã được hoàn lại"); }
    catch (e) { notify(e.message, "err"); }
  }
  return (
    <>
      <OrderView o={o} />
      <div style={{ marginTop: 16 }} className="inline">
        {o.canCancel && <button className="btn red" onClick={cancel}>Hủy đơn</button>}
        {o.status === "CHO_THANH_TOAN" && <a className="btn light" href={`/thanh-toan-gia-lap?order=${o.orderCode}&amount=${o.total}`}>Thanh toán lại</a>}
      </div>
    </>
  );
}
