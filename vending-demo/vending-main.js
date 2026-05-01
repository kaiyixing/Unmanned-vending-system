/**
 * 无人售货系统 - 黏土风格演示页面交互脚本
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1. 配置 & 常量
    const SELECTORS = {
        header: '#header',
        backToTop: '.back-to-top',
        aosElements: '[data-aos]',
        customCursor: '.custom-cursor',
        pointerElements: 'a, button, .squish-on-hover, .nav-link, .navbar-toggler, .magnetic-element',
        currentYear: '#currentYear',
        counters: '.counter-value',
        magneticElements: '.magnetic-element',
    };

    const CLASSES = {
        headerScrolled: 'is-scrolled',
        backToTopVisible: 'is-visible',
        aosVisible: 'is-visible',
        cursorActive: 'is-active',
        cursorPointer: 'is-pointer',
        cursorMagnetic: 'is-magnetic'
    };

    const SETTINGS = {
        scrollOffset: 50,
        magneticForce: 0.2,
        desktopBreakpoint: 992,
        counterAnimationDuration: 3000
    };

    // 2. 缓存 DOM 元素
    const header = document.querySelector(SELECTORS.header);
    const backToTop = document.querySelector(SELECTORS.backToTop);
    const currentYearEl = document.querySelector(SELECTORS.currentYear);

    // 3. 工具函数
    function observeElements(elements, callback, options = { threshold: 0.1 }) {
        const observer = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    callback(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        }, options);
        elements.forEach(el => observer.observe(el));
    }

    // 4. 自定义光标
    function initCustomCursor() {
        const customCursor = document.querySelector(SELECTORS.customCursor);
        if (!customCursor) return;

        const cursorDot = customCursor.querySelector('.cursor-dot');
        const cursorOutline = customCursor.querySelector('.cursor-outline');
        const pointerElements = document.querySelectorAll(SELECTORS.pointerElements);

        let cursorState = { x: 0, y: 0 };
        let previousCursorState = { x: 0, y: 0 };
        let isMoving = false;

        window.addEventListener('mousemove', () => {
            if (!isMoving) {
                customCursor.classList.add(CLASSES.cursorActive);
                isMoving = true;
            }
        }, { once: true });

        window.addEventListener('mousemove', e => {
            cursorState.x = e.clientX;
            cursorState.y = e.clientY;
        });

        const lerp = (a, b, n) => (1 - n) * a + n * b;

        const renderCursor = () => {
            previousCursorState.x = lerp(previousCursorState.x, cursorState.x, 0.15);
            previousCursorState.y = lerp(previousCursorState.y, cursorState.y, 0.15);

            cursorDot.style.transform = `translate3d(${cursorState.x}px, ${cursorState.y}px, 0)`;
            cursorOutline.style.transform = `translate3d(${previousCursorState.x}px, ${previousCursorState.y}px, 0)`;

            requestAnimationFrame(renderCursor);
        };
        requestAnimationFrame(renderCursor);

        pointerElements.forEach(el => {
            el.addEventListener('mouseenter', () => customCursor.classList.add(CLASSES.cursorPointer));
            el.addEventListener('mouseleave', () => customCursor.classList.remove(CLASSES.cursorPointer));
        });
    }

    // 5. 磁性按钮效果
    function initMagneticButtons() {
        if (window.innerWidth < SETTINGS.desktopBreakpoint) return;

        const magneticElements = document.querySelectorAll(SELECTORS.magneticElements);
        const customCursor = document.querySelector(SELECTORS.customCursor);

        magneticElements.forEach(el => {
            el.addEventListener('mousemove', function(e) {
                const { clientX, clientY } = e;
                const { left, top, width, height } = this.getBoundingClientRect();
                const x = (clientX - left - width / 2) * SETTINGS.magneticForce;
                const y = (clientY - top - height / 2) * SETTINGS.magneticForce;
                this.style.transform = `translate3d(${x}px, ${y}px, 0)`;

                if (customCursor) customCursor.classList.add(CLASSES.cursorMagnetic);
            });

            el.addEventListener('mouseleave', function() {
                this.style.transform = 'translate3d(0, 0, 0)';
                if (customCursor) customCursor.classList.remove(CLASSES.cursorMagnetic);
            });
        });
    }

    // 6. 导航栏滚动效果
    function initHeaderScroll() {
        if (!header) return;
        const handleScroll = () => {
            header.classList.toggle(CLASSES.headerScrolled, window.scrollY > SETTINGS.scrollOffset);
        };
        window.addEventListener('scroll', handleScroll);
        handleScroll();
    }

    // 7. 返回顶部按钮
    function initBackToTop() {
        if (!backToTop) return;
        const handleScroll = () => {
            backToTop.classList.toggle(CLASSES.backToTopVisible, window.scrollY > 300);
        };
        window.addEventListener('scroll', handleScroll);
        backToTop.addEventListener('click', (e) => {
            e.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // 8. 滚动动画 (AOS 替代)
    function initScrollAnimations() {
        const aosElements = document.querySelectorAll(SELECTORS.aosElements);
        if (aosElements.length) {
            observeElements(aosElements, el => el.classList.add(CLASSES.aosVisible));
        }
    }

    // 9. 数字计数器动画
    function initCounters() {
        const counters = document.querySelectorAll(SELECTORS.counters);
        if (!counters.length) return;

        const animateCounter = (el) => {
            const target = parseInt(el.dataset.count, 10);
            if (isNaN(target)) return;

            let current = 0;
            const increment = target / (SETTINGS.counterAnimationDuration / 16);

            const updateCount = () => {
                current += increment;
                if (current < target) {
                    el.textContent = Math.ceil(current).toLocaleString();
                    requestAnimationFrame(updateCount);
                } else {
                    el.textContent = target.toLocaleString();
                    // 添加 + 号
                    if (target > 1000) {
                        el.textContent = target.toLocaleString() + '+';
                    }
                }
            };
            updateCount();
        };

        observeElements(counters, animateCounter);
    }

    // 10. 设置当前年份
    function initCurrentYear() {
        if (currentYearEl) {
            currentYearEl.textContent = new Date().getFullYear();
        }
    }

    // 11. 触摸设备修复
    function initTouchHoverFix() {
        document.addEventListener('touchstart', function() {}, { passive: true });
    }

    // 12. 平滑滚动到锚点
    function initSmoothScroll() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                if (href === '#') return;

                const target = document.querySelector(href);
                if (target) {
                    e.preventDefault();
                    const headerHeight = header ? header.offsetHeight : 0;
                    const targetPosition = target.offsetTop - headerHeight - 20;

                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });

                    // 关闭移动端导航菜单
                    const navbarCollapse = document.querySelector('.navbar-collapse');
                    if (navbarCollapse && navbarCollapse.classList.contains('show')) {
                        const bsCollapse = bootstrap.Collapse.getInstance(navbarCollapse);
                        if (bsCollapse) bsCollapse.hide();
                    }
                }
            });
        });
    }

    // 13. 活跃导航链接高亮
    function initActiveNavLink() {
        const sections = document.querySelectorAll('section[id]');
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');

        window.addEventListener('scroll', () => {
            let current = '';
            const headerHeight = header ? header.offsetHeight : 0;

            sections.forEach(section => {
                const sectionTop = section.offsetTop - headerHeight - 100;
                if (window.scrollY >= sectionTop) {
                    current = section.getAttribute('id');
                }
            });

            navLinks.forEach(link => {
                link.classList.remove('active');
                if (link.getAttribute('href') === `#${current}`) {
                    link.classList.add('active');
                }
            });
        });
    }

    // 初始化
    try {
        initCustomCursor();
        initMagneticButtons();
        initHeaderScroll();
        initBackToTop();
        initScrollAnimations();
        initCounters();
        initCurrentYear();
        initTouchHoverFix();
        initSmoothScroll();
        initActiveNavLink();
    } catch (error) {
        console.error('初始化过程中发生错误:', error);
    }
});
