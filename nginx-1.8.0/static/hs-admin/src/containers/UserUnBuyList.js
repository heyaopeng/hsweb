import React from 'react'
import DocumentTitle from 'react-document-title'
import './user-list.css'
import req from 'superagent'
// import { Link } from 'react-router'

class UserUnBuyList extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      users: [],
      size: 10,
      day: this.props.location.query.day,
      checked: []
    }
  }

  componentDidMount() {
    req
      .get(
        '/uclee-backend-web/userUnBuyList?day=' + this.props.location.query.day
      )
      .end((err, res) => {
        if (err) {
          return err
        }
        var data = JSON.parse(res.text)
        this.setState({
          users: data.users,
          size: data.users.length
        })
      })
  }
  _send = userId => {
    req
      .get('/uclee-backend-web/sendUnbuyMsg?userId=' + userId)
      .end((err, res) => {
        if (err) {
          return err
        }
        if (res.body) {
          alert('发送成功')
          window.location =
            '/user-unbuy-list?day=' + this.props.location.query.day
        } else {
          alert('网络繁忙，请稍后重试')
        }
      })
  }
  _change = e => {
    this.setState({
      day: e.target.value
    })
  }

  _search = () => {
    window.location = window.location = '/user-unbuy-list?day=' + this.state.day
  }
   _sendAll=()=>{
    if(this.state.checked.length===0){
      alert("请选择要批量发送的用户");
      return;
    }
    var ret = true;
    for (var i in this.state.checked)
    {
      console.log(this.state.checked[i]);
      req
      .get('/uclee-backend-web/sendUnbuyMsg?userId=' + this.state.checked[i])
      .end((err, res) => {
        if (err) {
          return err
        }
        ret = ret && res.body;
      })
    }
    if (ret) {
          alert('发送成功')
          window.location =
            '/user-unbuy-list?day=' + this.props.location.query.day
        } else {
          alert('网络繁忙，请稍后重试')
        }
  }

  render() {
    var list = this.state.users.map((item, index) => {
      return (
        <tr key={index}>
          <td>
            <input
              type="checkbox"
              checked={this.state.checked.indexOf(item.userId) !== -1}
              onChange={this._checkChange.bind(this, item.userId)}
            />
          </td>
          <td>{item.nickName}</td>
          <td>{item.lastBuyStr ? item.lastBuyStr : '从未消费'}</td>
          <td>
            <button
              className="btn btn-primary"
              onClick={this._send.bind(this, item.userId)}
            >
              发送短信
            </button>
          </td>
        </tr>
      )
    })
    return (
      <DocumentTitle title="用户列表">
        <div className="user-list">
          <div className="user-list-add">
            <label className="control-label col-md-3">距今天数超过：</label>
            <input
              type="text"
              name="day"
              value={this.state.day}
              onChange={this._change}
            />
            <div className="btn btn-primary" onClick={this._search}>
              搜索
            </div>
          </div>
          <button className="btn btn-primary pull-left" onClick={this._sendAll}>批量发送</button>
          <table className="table table-bordered table-striped">
            <thead>
              <tr>
                <th>
                  <input type="checkbox" onChange={this._checkChangeAll} checked={this.state.checked.length === this.state.users.length&&this.state.checked.length>0} />全选
                </th>
                <th>昵称</th>
                <th>最后一次消费</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {list}
            </tbody>
          </table>
        </div>
      </DocumentTitle>
    )
  }

  _checkChange = (id, e) => {
    if (e.target.checked) {
      // push it
      this.setState(prevState => {
        return {
          checked: prevState.checked.concat(id)
        }
      })
      return
    }

    // remove it
    this.setState(prevState => {
      return {
        checked: prevState.checked.filter(item => {
          return item !== id
        })
      }
    })
  }

  _checkChangeAll = e => {
    if (e.target.checked) {
      this.setState(prevState => {
        return {
          checked: prevState.users.map(item => item.userId)
        }
      })
      return
    }

    this.setState(prevState => {
      return {
        checked: []
      }
    })
  }
}

export default UserUnBuyList
