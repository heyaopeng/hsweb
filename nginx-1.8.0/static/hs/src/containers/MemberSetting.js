import React from 'react'
import DocumentTitle from 'react-document-title'
import './member-setting.css'

import { browserHistory } from 'react-router'

import ErrorMsg from '../components/ErrorMsg'
import Noti from '../components/Noti'

import fto from 'form_to_object'
import validator from 'validator'
import req from 'superagent'

import moment from 'moment'

class MemberSetting extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      error: '',
      showNoti: false,

      cName: '',
      cMobileNumber: '',
      code: '',
      cBirthday: '',
      bIsLunar: '',
      cVipCode: '',

      type: '1',
      resultmsg: '保存成功',

      time: 60,
      fetchingCode: false
    }

    this.tick = null
  }

  componentDidMount() {
    req.get('/uclee-user-web/getVipInfo').end((err, res) => {
      if (err) {
        return err
      }
      var d = res.body

      this.setState({
        cName: d.cName,
        cMobileNumber: d.cMobileNumber,
        cBirthday: moment(d.cBirthday).format('YYYY-MM-DD'),
        bIsLunar: d.bIsLunar,
        cVipCode: d.cVipCode
      })
    })
  }

  componentWillUnmount() {
    clearInterval(this.tick)
  }

  render() {
    return (
      <DocumentTitle title="会员信息设置">
        <div className="member-setting container">
          <Noti visible={this.state.showNoti} text={this.state.resultmsg} />
          <div className="member-setting-tabs clearfix">
            <div
              className={
                'member-setting-tab' +
                  (this.state.type === '1' ? ' active' : '')
              }
              onClick={this._tab.bind(this, '1')}
            >
              有线下会员卡
            </div>
            <div
              className={
                'member-setting-tab' +
                  (this.state.type === '2' ? ' active' : '')
              }
              onClick={this._tab.bind(this, '2')}
            >
              无线下会员卡
            </div>
          </div>
          <form
            className="form-horizontal member-setting-form"
            onSubmit={this._submit}
          >
            {this.state.type === '2'
              ? <div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      姓名
                    </label>
                    <div className="col-xs-9">
                      <input
                        type="text"
                        className="form-control"
                        placeholder="请输入姓名"
                        name="cName"
                        value={this.state.cName}
                        onChange={this._change}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      手机号码
                    </label>
                    <div className="col-xs-9">
                      <input
                        type="tel"
                        name="cMobileNumber"
                        className="form-control"
                        placeholder="请输入手机号码"
                        value={this.state.cMobileNumber}
                        onChange={this._change}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      验证码
                    </label>
                    <div className="col-xs-5">
                      <input
                        type="tel"
                        name="code"
                        className="form-control"
                        placeholder="请输入验证码"
                        value={this.state.code}
                        onChange={this._change}
                      />
                    </div>
                    <div className="col-xs-4">
                      <button
                        type="button"
                        className="btn btn-default member-setting-code-btn"
                        onClick={this._getCode}
                        disabled={this.state.fetchingCode}
                      >
                        {this.state.fetchingCode
                          ? `(${this.state.time})`
                          : '获取验证码'}
                      </button>
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      生日
                    </label>
                    <div className="col-xs-9">
                      <input
                        type="date"
                        name="cBirthday"
                        className="form-control"
                        value={this.state.cBirthday}
                        onChange={this._change}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      生日类型
                    </label>
                    <div className="col-xs-9">
                      <select
                        name="bIsLunar"
                        className="form-control"
                        value={this.state.bIsLunar}
                        onChange={this._change}
                      >
                        <option value="0">公历</option>
                        <option value="1">农历</option>
                      </select>
                    </div>
                  </div>
                </div>
              : <div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      手机号码
                    </label>
                    <div className="col-xs-9">
                      <input
                        type="tel"
                        name="cMobileNumber"
                        className="form-control"
                        placeholder="请输入手机号码"
                        value={this.state.cMobileNumber}
                        onChange={this._change}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-3 trim-right">
                      验证码
                    </label>
                    <div className="col-xs-5">
                      <input
                        type="tel"
                        name="code"
                        className="form-control"
                        placeholder="请输入验证码"
                        value={this.state.code}
                        onChange={this._change}
                      />
                    </div>
                    <div className="col-xs-4">
                      <button
                        type="button"
                        className="btn btn-default member-setting-code-btn"
                        onClick={this._getCode}
                        disabled={this.state.fetchingCode}
                      >
                        {this.state.fetchingCode
                          ? `(${this.state.time})`
                          : '获取验证码'}
                      </button>
                    </div>
                  </div>
                </div>}

            <ErrorMsg msg={this.state.error} />

            <div className="form-btn-wrap">
              <button type="submit" className="btn btn-success btn-block">
                保存
              </button>
            </div>
          </form>
        </div>
      </DocumentTitle>
    )
  }

  _tab = t => {
    this.setState({
      type: t,
      code:""
    })
  }

  _change = e => {
    this.setState({
      [e.target.name]: e.target.value
    })
  }

  _getCode = () => {
    if (!validator.isMobilePhone(this.state.cMobileNumber, 'zh-CN')) {
      return this.setState({
        error: '请输入正确的手机号码'
      })
    }
    this.setState({
      fetchingCode: true
    })
    req
      .get('/uclee-user-web/verifyCode')
      .query({
        phone: this.state.cMobileNumber
      })
      .end((err, res) => {
        if (err) {
          return err
        }
        this._tick()
      })
  }

  _tick = () => {
    this.tick = setInterval(() => {
      if (this.state.time <= 0) {
        this.setState({
          fetchingCode: false,
          time: 60
        })
        clearInterval(this.tick)
        return
      }

      this.setState(prevState => {
        return {
          time: prevState.time - 1
        }
      })
    }, 1000)
  }

  _submit = e => {
    e.preventDefault()
    var data = fto(e.target)
    if (!data.cMobileNumber) {
      return this.setState({
        error: '请输入手机号码'
      })
    }

    if (!data.code) {
      return this.setState({
        error: '请输入验证码'
      })
    }

    if (!/^\d{6}$/.test(data.code)) {
      return this.setState({
        error: '验证码不正确'
      })
    }

    if (!validator.isMobilePhone(data.cMobileNumber, 'zh-CN')) {
      return this.setState({
        error: '请输入正确的手机号码'
      })
    }

    if (this.state.type === '2') {
      if (!data.cName) {
        return this.setState({
          error: '请输入姓名'
        })
      }

      if (!data.cBirthday) {
        return this.setState({
          error: '请输入生日'
        })
      }

      if (!data.bIsLunar) {
        return this.setState({
          error: '请选择生日类型'
        })
      }
    }

    req.post('/uclee-user-web/addVipInfo').send(data).end((err, res) => {
      if (err) {
        return err
      }

      if (res.body.result === 'fail') {
        this.setState({
          error: res.body.reason
        })
        return
      }

      this.setState({
        showNoti: true
      })

      setTimeout(() => {
        browserHistory.replace({
          pathname: '/member-card'
        })
      }, 2500)
    })

    this.setState({
      error: ''
    })
  }
}

export default MemberSetting
