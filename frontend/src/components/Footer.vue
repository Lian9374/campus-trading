<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="footer-col">
        <h4 class="footer-logo">
          <el-icon :size="20"><ShoppingBag /></el-icon>
          校园二手
        </h4>
        <p class="footer-desc">
          校园二手交易平台，让闲置流转，让价值延续。安全、便利的大学生二手买卖社区。
        </p>
      </div>
      <div class="footer-col">
        <h5>快速链接</h5>
        <ul>
          <li><router-link to="/">首页</router-link></li>
          <li><router-link to="/publish">发布商品</router-link></li>
          <li><router-link to="/my/orders">我的订单</router-link></li>
        </ul>
      </div>
      <div class="footer-col">
        <h5>帮助与支持</h5>
        <ul>
          <li><a href="#" @click.prevent="openHelp('guide')">交易指南</a></li>
          <li><a href="#" @click.prevent="openHelp('safety')">安全提示</a></li>
          <li><a href="#" @click.prevent="openHelp('contact')">联系我们</a></li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2024 校园二手 Campus Trading. All rights reserved.</p>
    </div>

    <!-- 帮助弹窗 -->
    <el-dialog v-model="showHelp" :title="helpTitle" width="520px" :before-close="() => showHelp = false">
      <!-- 交易指南 -->
      <div v-if="helpType === 'guide'" class="help-content">
        <h4>📖 买家指南</h4>
        <ol>
          <li>浏览首页或在搜索框输入关键词查找心仪商品</li>
          <li>点击商品卡片查看详情、卖家信息和评价</li>
          <li>可先通过 <strong>"联系卖家"</strong> 按钮私信咨询</li>
          <li>确定购买后点击 <strong>"立即购买"</strong> 下单</li>
          <li>卖家确认后与卖家协商面交时间和地点</li>
          <li>收货后在订单页点击 <strong>"确认收货"</strong> 完成交易</li>
        </ol>
        <h4 style="margin-top: 20px">📦 卖家指南</h4>
        <ol>
          <li>点击导航栏 <strong>"发布商品"</strong> 填写商品信息</li>
          <li>上传清晰的商品照片，如实描述商品状况</li>
          <li>收到订单后尽快 <strong>"确认订单"</strong></li>
          <li>与买家协商面交或快递方式</li>
          <li>交易完成后双方可互相评价</li>
        </ol>
        <h4 style="margin-top: 20px">💡 小贴士</h4>
        <ul>
          <li>关注你喜欢的卖家，第一时间收到上新通知</li>
          <li>使用校区筛选功能找到同校的交易伙伴</li>
          <li>如实评价有助于建立良好的社区信用体系</li>
        </ul>
      </div>

      <!-- 安全提示 -->
      <div v-if="helpType === 'safety'" class="help-content">
        <h4>🔒 交易安全须知</h4>
        <ol>
          <li><strong>选择校内面交：</strong>尽量在校园内公共场所（如图书馆、食堂）进行交易</li>
          <li><strong>当面验货：</strong>交易时仔细检查商品是否与描述一致</li>
          <li><strong>不要提前付款：</strong>建议面交时一手交钱一手交货</li>
          <li><strong>保护个人信息：</strong>不要随意透露身份证号、银行卡号等敏感信息</li>
          <li><strong>警惕诈骗：</strong>价格明显低于市场价的商品需提高警惕</li>
        </ol>
        <h4 style="margin-top: 20px">🚫 禁止交易物品</h4>
        <ul>
          <li>违禁药品、管制刀具等危险品</li>
          <li>假冒伪劣、侵犯知识产权的商品</li>
          <li>学校明令禁止在宿舍使用的电器</li>
          <li>任何违法违规物品</li>
        </ul>
        <h4 style="margin-top: 20px">⚠️ 遇到问题</h4>
        <p>如果遇到虚假信息、违规商品或交易纠纷，请使用举报功能或联系平台管理员。</p>
      </div>

      <!-- 联系我们 -->
      <div v-if="helpType === 'contact'" class="help-content">
        <h4>📧 联系方式</h4>
        <ul>
          <li><strong>反馈邮箱：</strong>campus-trading@example.com</li>
          <li><strong>问题反馈：</strong>通过平台举报功能提交</li>
        </ul>
        <h4 style="margin-top: 20px">ℹ️ 关于我们</h4>
        <p>校园二手是一个面向大学生的校内二手交易平台，致力于让闲置物品在校园内高效流转，减少浪费，促进可持续消费。</p>
        <p style="margin-top: 8px">本平台不参与实际交易，仅提供信息展示和沟通渠道。交易双方请自行协商并注意安全。</p>
      </div>

      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

const showHelp = ref(false)
const helpType = ref('guide')
const helpTitle = ref('交易指南')

const helpTitles = { guide: '交易指南', safety: '安全提示', contact: '联系我们' }

function openHelp(type) {
  helpType.value = type
  helpTitle.value = helpTitles[type] || '帮助'
  showHelp.value = true
}
</script>

<style scoped>
.footer {
  background: #064e3b;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 64px;
}

.footer-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 36px;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr;
  gap: 48px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--text-lg);
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
}

.footer-desc {
  font-size: var(--text-sm);
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.6);
}

.footer-col h5 {
  font-size: var(--text-sm);
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
}

.footer-col ul {
  list-style: none;
  padding: 0;
}

.footer-col li {
  margin-bottom: 10px;
}

.footer-col a {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.6);
  transition: color var(--transition-fast);
}

.footer-col a:hover {
  color: #fff;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px 24px;
  text-align: center;
  font-size: var(--text-xs);
  color: rgba(255, 255, 255, 0.4);
}

/* Help Content */
.help-content {
  font-size: var(--text-sm);
  color: var(--color-text);
  line-height: 1.8;
}

.help-content h4 {
  font-size: var(--text-base);
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--color-text);
}

.help-content ol, .help-content ul {
  padding-left: 20px;
}

.help-content li {
  margin-bottom: 6px;
}

@media (max-width: 768px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 36px 20px 28px;
  }
}
</style>
