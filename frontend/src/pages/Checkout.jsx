import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, money } from "../api.js";
import { useApp } from "../state.jsx";

// UC09 đặt hàng (Hình 2.4): kiểm tra lại giá, tồn kho -> phí giao -> chọn phương thức -> tạo đơn
export default function Checkout() {
  const { session, notify, refreshCart } = useApp();
  const nav = useNavigate();
  const [addrs, setAddrs] = useState([]);
  const [addrId, setAddrId] = useState(null);
  const [newAddr, setNewAddr] = useState({ receiverName: "", receiverPhone: "", addressLine: "" });
  const [summary, setSummary] = useState(null);
  const [method, setMethod] = useState("COD");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!session) return;
    api("/addresses").then((a) => { setAddrs(a); if (a.length) setAddrId(a[0].id); });
    api("/orders/prepare", { method: "POST" }).then(setSummary).catch((e) => setError(e.message));
  }, [session]);
  if (!session) return <div className="panel">Vui lòng <Link to="/dang-nhap?next=/thanh-toan">đăng nhập</Link>.</div>;

  async function addAddress() {
    try { const a = await api("/addresses", { method: "POST", body: newAddr }); setAddrs(a); setAddrId(a[a.length - 1].id);
      setNewAddr({ receiverName: "", receiverPhone: "", addressLine: "" }); } catch (e) { notify(e.message, "err"); }
  }
  async function place() {
    setBusy(true);
    try {
      const r = await api("/orders", { method: "POST", body: { addressId: addrId, paymentMethod: method, expectedTotal: summary.total } });
      refreshCart();
      if (r.paymentUrl) nav(r.paymentUrl);              // ONLINE -> cổng thanh toán (giả lập khi demo)
      else { notify("Đặt hàng thành công: " + r.orderCode); nav("/don-hang/" + r.orderCode); }
    } catch (e) { setError(e.message); notify(e.message, "err"); } finally { setBusy(false); }
  }

  return (
    <div className="two">
      <div>
        <h1>Thanh toán</h1>
        {error && <div className="panel err-text" style={{ marginBottom: 12 }}>{error} — <Link to="/gio-hang">quay lại giỏ hàng</Link></div>}
        <div className="panel">
          <h2>Địa chỉ nhận hàng</h2>
          {addrs.map((a) => (
            <label key={a.id} className={"radio " + (addrId === a.id ? "on" : "")}>
              <input type="radio" checked={addrId === a.id} onChange={() => setAddrId(a.id)} />
              <span><b>{a.receiverName}</b> · {a.receiverPhone}<br /><span className="muted">{a.addressLine}</span></span>
            </label>
          ))}
          <details style={{ marginTop: 10 }}>
            <summary>Thêm địa chỉ mới</summary>
            {["receiverName:Họ tên người nhận", "receiverPhone:Số điện thoại", "addressLine:Địa chỉ"].map((x) => {
              const [k, l] = x.split(":");
              return <div className="field" key={k}><label>{l}</label><input value={newAddr[k]} onChange={(e) => setNewAddr({ ...newAddr, [k]: e.target.value })} /></div>;
            })}
            <button className="btn light" onClick={addAddress}>Lưu địa chỉ</button>
          </details>
        </div>
        <div className="panel" style={{ marginTop: 16 }}>
          <h2>Phương thức thanh toán</h2>
          <label className={"radio " + (method === "COD" ? "on" : "")}><input type="radio" checked={method === "COD"} onChange={() => setMethod("COD")} /> Thanh toán khi nhận hàng (COD)</label>
          <label className={"radio " + (method === "ONLINE" ? "on" : "")}><input type="radio" checked={method === "ONLINE"} onChange={() => setMethod("ONLINE")} /> Thanh toán trực tuyến (giữ hàng 15 phút)</label>
        </div>
      </div>
      <div className="panel summary">
        <h2>Đơn hàng</h2>
        {summary ? (
          <>
            <div className="line"><span>Tạm tính</span><span>{money(summary.subtotal)}</span></div>
            <div className="line"><span>Phí giao hàng</span><span>{Number(summary.shippingFee) === 0 ? "Miễn phí" : money(summary.shippingFee)}</span></div>
            <hr />
            <div className="line"><b>Tổng cộng</b><span className="total">{money(summary.total)}</span></div>
            <button className="btn" style={{ width: "100%", marginTop: 12, padding: 14 }} disabled={!addrId || busy} onClick={place}>ĐẶT HÀNG</button>
            {!addrId && <p className="err-text">Vui lòng thêm địa chỉ nhận hàng.</p>}
          </>
        ) : <p className="muted">{error ? "Không thể đặt hàng." : "Đang kiểm tra giỏ hàng..."}</p>}
      </div>
    </div>
  );
}
