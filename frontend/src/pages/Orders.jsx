import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, money, STATUS, fmtTime } from "../api.js";
import { useApp } from "../state.jsx";

// UC11 đơn của tôi
export default function Orders() {
  const { session } = useApp();
  const [list, setList] = useState(null);
  useEffect(() => { if (session) api("/orders").then(setList); }, [session]);
  if (!session) return <div className="panel">Vui lòng <Link to="/dang-nhap?next=/don-hang">đăng nhập</Link>.</div>;
  return (
    <>
      <h1>Đơn của tôi</h1>
      {!list ? <p className="muted">Đang tải...</p> : !list.length ? <div className="panel">Bạn chưa có đơn hàng nào.</div> : (
        <table>
          <thead><tr><th>Mã đơn</th><th>Ngày đặt</th><th>Số SP</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th></tr></thead>
          <tbody>{list.map((o) => (
            <tr key={o.orderCode}>
              <td><Link to={"/don-hang/" + o.orderCode}><b>{o.orderCode}</b></Link></td><td>{fmtTime(o.orderDate)}</td><td>{o.itemCount}</td>
              <td className="price">{money(o.total)}</td><td>{o.paymentMethod}</td><td><span className={"status " + o.status}>{STATUS[o.status]}</span></td>
            </tr>))}
          </tbody>
        </table>
      )}
    </>
  );
}
