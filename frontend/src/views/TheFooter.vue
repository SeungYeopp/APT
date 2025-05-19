<script setup>

</script>

<template>
    <div class="footer">
        <div class="footer-container">
            <h1 id="home" style="color: white;">APT</h1>
            <p>&copy; 2024 SSAFY 12기 13반 6조 강승엽 공예슬</p>
            <ul class="footer-links">
                <li><a href="#">Terms</a></li>
                <li><a href="#">Privacy</a></li>
                <li><a href="#">Security</a></li>
                <li><a href="#">Status</a></li>
                <li><a href="#">Docs</a></li>
                <li><a href="#">Contact</a></li>
                <li><a href="#">Manage cookies</a></li>
                <li><a href="#">Do not share my personal information</a></li>
            </ul>
        </div>
    </div>
</template>

<style scoped>
.footer {
    background-color: var(--eerie-black); /* 배경색 */
    color: var(--powder-blue); /* 텍스트 색상 */
    padding: 20px 0;
    text-align: center;
    font-size: 0.9rem;
    border-top: 1px solid #444; /* 상단 경계선 */
}

.footer-container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

.footer-logo {
    width: 24px;
    height: 24px;
}

.footer-links {
    list-style: none;
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    padding: 0;
    margin: 0;
}

.footer-links li {
    display: inline;
}

.footer-links a {
    color: var(--powder-blue);
    text-decoration: none;
    font-size: 0.85rem;
}

.footer-links a:hover {
    text-decoration: underline;
}
</style>