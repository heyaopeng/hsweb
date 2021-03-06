import React from 'react'
import DocumentTitle from 'react-document-title'
import hero from './member-center.jpg'
import './member-center.css'
import Icon from '../components/Icon'
import LinkGroup from '../components/LinkGroup'
import LinkGroupItem from '../components/LinkGroupItem'
import req from 'superagent'
import Navi from './Navi'
// import Footer from '../components/Footer'

class MemberCenter extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      nickName: '',
      point:0,
      serialNum:''
    }
  }

  componentDidMount() {
    req
      .get('/uclee-user-web/getUserInfo')
      .query({
        t: new Date().getTime()
      })
      .end((err, res) => {
        this.setState(res.body)
      })
  }

  render() {
    return (
      <DocumentTitle title="会员中心">
        <div className="member-center">
          <div className="member-center-hero">
            <span className="member-center-check-in" onClick={() => { 
              req
                .get('/uclee-user-web/signInHandler')
                .end((err, res) => {
                  var data = JSON.parse(res.text);
                  if(data.existed){
                    alert("今天已经签到过了哦~")
                    return;
                  }
                  if(data.result){
                    this.setState({
                      point : Number(this.state.point) + Number(data.point)
                    })
                    alert("签到成功，积分+" + data.point)
                    return;
                  }
                  if(!data.result){
                    alert("网络繁忙请稍后重试")
                    return;
                  }
                  
                })

            }}>签到获取积分</span>

            <img src={hero} alt=""/>
            <div className="member-center-info">
              <div>尊贵的 {this.state.nickName}</div>
              {/*<div>您拥有本店积分：{0}</div>*/}
            </div>
          </div>

          <div className="member-center-status clearfix">
            <div className="member-center-status-item">
                <span className="member-center-status-title"><Icon name="database" className="member-center-status-icon" />积分</span>
                <span>{this.state.point || '0'}</span>
            </div>
            <div className="member-center-status-item" onClick={() => { window.location='/member-card' }}>
              <a href="/member-card">
                <span className="member-center-status-title"><Icon name="money" className="member-center-status-icon" />余额</span>
                <span>{this.state.balance || '0'}</span>
              </a>
            </div>
            <div className="member-center-status-item" onClick={() => { window.location='/coupon'  }}>
                <span className="member-center-status-title"><Icon name="ticket" className="member-center-status-icon" />电子券</span>
                <span>{this.state.couponAmount || '0'}</span>
            </div>
          </div>

          <div className="member-center-orders clearfix">
            <div className="member-center-order" onClick={() => { window.location='/unpay-order-list' }}>
              <a href="#">
                <Icon name="smile-o" className="member-center-order-icon" />
                <span>待付款</span>
              </a>
            </div>
            <div className="member-center-order" onClick={() => { window.location='/order-list?isEnd=0' }}>
              <a href="#">
                <Icon name="smile-o" className="member-center-order-icon" />
                <span>制作配送中</span>
              </a>
            </div>
            <div className="member-center-order" onClick={() => { window.location='/order-list?isEnd=1'  }}>
              <a href="#">
                <Icon name="smile-o" className="member-center-order-icon" />
                <span>已结单</span>
              </a>
            </div>
          </div>

          <LinkGroup>
            <LinkGroupItem icon="smile-o" text="全部订单" onClick={() => { window.location='/order-list'  }}/>
            <LinkGroupItem icon="smile-o" text="我的购物车" onClick={() => { window.location='/cart'}}/>
          </LinkGroup>

          <LinkGroup>
            <LinkGroupItem icon="smile-o" text="我的会员卡" href="/member-card"/>
            <LinkGroupItem icon="smile-o" text="会员卡明细" href="/recharge-list"/>
            <LinkGroupItem icon="smile-o" text="积分抽奖" href="/lottery"/>
            <LinkGroupItem icon="smile-o" text="我的优惠券" onClick={() => { window.location='/coupon' }}/>
            <LinkGroupItem icon="smile-o" text="分销中心" onClick={() => { window.location='/distribution-center?serialNum='+this.state.serialNum+'&merchantCode='+localStorage.getItem('merchantCode')}}/>
          </LinkGroup>

          <LinkGroup>
            <LinkGroupItem icon="smile-o" text="收货地址管理" href="/address"/>
          </LinkGroup>
          <Navi query={this.props.location.query}/>
        </div>
      </DocumentTitle>
      )
  }
}

export default MemberCenter
