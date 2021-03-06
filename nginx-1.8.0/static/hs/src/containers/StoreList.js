/* global wx */
import './store-list.css'
import React from 'react'
import DocumentTitle from 'react-document-title'

// req 用于发送 AJAX 请求
import req from 'superagent'

var qq = window.qq

class StoreList extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			stores: [],
			userLocation: {
				latitude: null,
				longitude: null
			}
		}
		this.lat = 23.12463
		this.lng = 113.36199
	}

	componentDidMount() {
		req.get('/uclee-user-web/storeList').end((err, res) => {
			if (err) {
				return err
			}

			this.setState({
				stores: res.body
			})
		})

		req
			.get('/uclee-user-web/wxConfig')
			.query({
				url: window.location.href.split('#')[0]
			})
			.end((err, res) => {
				var c = JSON.parse(res.text)
				wx.config({
					debug: false,
					appId: c.appId,
					timestamp: c.timestamp,
					nonceStr: c.noncestr,
					signature: c.signature,
					jsApiList: ['getLocation']
				})

				wx.ready(() => {
					wx.getLocation({
						type: 'gcj02',
						success: res => {
							var latitude = res.latitude
							var longitude = res.longitude
							this.setState({
								userLocation: {
									latitude: latitude,
									longitude: longitude
								}
							})
						}
					})
				})
			})
	}
	render() {
		var preItems=this.state.stores.map((item, index) => {
			var distance = Math.round(
				qq.maps.geometry.spherical.computeDistanceBetween(
					new qq.maps.LatLng(
						this.state.userLocation.latitude,
						this.state.userLocation.longitude
					),
					new qq.maps.LatLng(item.latitude, item.longitude)
				) / 100
			)/10
			item.distance=distance
			return item
		})
		console.log("before sort:"+JSON.stringify(preItems))
		preItems.sort((a,b)=>{
			var x = a.distance; var y = b.distance
	        return ((x < y) ? -1 : ((x > y) ? 1 : 0))
		})
		console.log(JSON.stringify(preItems))

		var items = preItems.map((item, index) => {
			var distance = item.distance
			return (
				<div className="store-list" key={index}>
					<div
						className="store-list-item"
						onClick={this._pick.bind(
							this,
							item.storeName,
							item.storeId,
							item.latitude,
							item.longitude
						)}
					>
						<div className="store-list-item-top">
							<div className="name">{item.storeName}</div>
							<div className="distance">
								{distance > 10 ? '>10' : distance}
								km
							</div>
						</div>
						<div className="store-list-item-bottom">
							<div className="addr">
								{item.province}{item.city}{item.region}{item.addrDetail}
							</div>
							<div className="fa fa-chevron-right right" />
						</div>
					</div>
				</div>
			)
		})
		return (
			<DocumentTitle title="选择店铺">
				<div className="store">
					<div className="store-logo">
						<img src="./images/brand.jpg" className="store-logo-image" alt=""/>
						<div className="store-logo-text">洪石烘焙</div>
					</div>
					<div className="store-select">请选择要进入的店：</div>
					{items}
					<div className="bottom-text">
						O(∩_∩)O 啊哦，没有更多店铺啦~~~
					</div>
				</div>
			</DocumentTitle>
		)
	}
	_pick = (storeName, storeId, latitude, longitude) => {
		localStorage.setItem('storeName', storeName)
		localStorage.setItem('storeId', storeId)
		localStorage.setItem('latitude', latitude)
		localStorage.setItem('longitude', longitude)

		window.location = localStorage.getItem('store_id_prev_href')
	}
}

export default StoreList
