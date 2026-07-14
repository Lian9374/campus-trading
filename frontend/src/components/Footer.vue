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
          <li><a href="#">交易指南</a></li>
          <li><a href="#">安全提示</a></li>
          <li><a href="#">举报反馈</a></li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2024 校园二手 Campus Trading. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup>
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

@media (max-width: 768px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 36px 20px 28px;
  }
}
</style>
