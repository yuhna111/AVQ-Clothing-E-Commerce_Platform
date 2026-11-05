import React, { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { api, money, STATUS, fmtTime } from "../api.js";
import { useApp } from "../state.jsx";
import { OrderView } from "../pages/OrderDetail.jsx";

const FILTERS = ["", "CHO_THANH_TOAN", "CHO_XAC_NHAN", "DANG_GIAO", "DA_GIAO", "DA_HUY"];

// UC18 xem đơn, UC19 cập nhật trạng thái (DT-04: chỉ hiện trạng thái kế tiếp hợp lệ)
export default function AdminOrders() {
  const { session, notify } = useApp();
  const [status, setStatus] = useState("CHO_XAC_NHAN");
  const [list, setList] = useState([]);
  const [sel, setSel] = useState(null);
  const [next, setNext] = useState("");
  const [note, setNote] = useState("");
  const load = () => api("/admin/orders", { params: { status } }).then(setList);
  useEffect(() => { if (session && session.role === "STAFF") load(); }, [status, session]);
  if (!session || session.role !== "STAFF") return <Navigate to="/admin" />;

  const opts = (o) => (o.status === "CHO_THANH_TOAN" ? [] : o.nextStatuses);   // DT-03: đơn chờ thanh toán do hệ thống xử lý
  async function open(code) { const o = await api("/admin/orders/" + code); setSel(o); setNext(opts(o)[0] || ""); setNote(""); }
  async function update() {
    try {
      const o = await api(`/admin/orders/${sel.orderCode}/status`, { method: "PATCH", body: { status: next, note } });
      setSel(o); setNext(opts(o)[0] || ""); setNote(""); notify("Đã chuyển sang: " + STATUS[o.status]); load();
    } catch (e) { notify(e.message, "err"); }
  }
  return (
    <>
      <h1>Quản lý đơn hàng</h1>
      <div className="chips" style={{ marginBottom: 12 }}>
        {FILTERS.map((s) => <button key={s} className={"chip " + (status === s ? "on" : "")} onClick={() => { setStatus(s); setSel(null); }}>{s ? STATUS[s] : "Tất cả"}</button>)}
      </div>
      <div className="two">
        <div>
          <table>
            <thead><tr><th>Mã đơn</th><th>Ngày đặt</th><th>Khách</th><th>Tổng</th><th>Trạng thái</th></tr></thead>
            <tbody>
              {list.map((o) => (
                <tr key={o.orderCode} onClick={() => open(o.orderCode)} style={{ cursor: "pointer", background: sel && sel.orderCode === o.orderCode ? "#eaf7fd" : "" }}>
                  <td><b>{o.orderCode}</b></td><td>{fmtTime(o.orderDate)}</td><td>{o.receiverName}</td><td>{money(o.total)}</td>
                  <td><span className={"status " + o.status}>{STATUS[o.status]}</span></td>
                </tr>))}
              {!list.length && <tr><td colSpan="5" className="muted">Không có đơn nào.</td></tr>}
            </tbody>
          </table>
        </div>
        <div>
          {sel ? (
            <>
              <div className="panel" style={{ marginBottom: 16 }}>
                <h2>Chuyển trạng thái</h2>
                {opts(sel).length ? (
                  <>
                    <div className="field"><label>Trạng thái mới</label>
                      <select value={next} onChange={(e) => setNext(e.target.value)}>
                        {opts(sel).map((s) => <option key={s} value={s}>{STATUS[s]}</option>)}
                      </select></div>
                    <div className="field"><label>{next === "DANG_GIAO" ? "Mã vận đơn" : next === "DA_HUY" ? "Lý do hủy (bắt buộc)" : "Ghi chú"}</label>
                      <input value={note} onChange={(e) => setNote(e.target.value)} /></div>
                    <button className="btn" onClick={update}>Cập nhật</button>
                  </>
                ) : <p className="muted">{sel.status === "CHO_THANH_TOAN" ? "Đơn đang chờ khách thanh toán; hệ thống tự hủy sau 15 phút." : "Đơn đã ở trạng thái kết thúc."}</p>}
              </div>
              <OrderView o={sel} />
            </>
          ) : <div className="panel muted">Chọn một đơn để xem chi tiết.</div>}
        </div>
      </div>
    </>
  );
}
