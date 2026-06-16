// [brandLogic.js] 전역 변수(selectedMainMoods 등)는 common.js에 선언되어 있다고 가정합니다.

// ---------------------------------------------------------
// 1. 브랜드기획 동기화 - 무드태그 태그 로직
// ---------------------------------------------------------
function handleMoodTagClick(clickedTag, opposingTag) {
    const currentBtn = document.getElementById('main-tag-' + clickedTag);
    const oppoBtn = document.getElementById('main-tag-' + opposingTag);

    if(!currentBtn || !oppoBtn || currentBtn.classList.contains('disabled-tag')) return;

    if(currentBtn.classList.contains('selected-tag')) {
        currentBtn.classList.remove('selected-tag');
        oppoBtn.classList.remove('disabled-tag');
        selectedMainMoods = selectedMainMoods.filter(t => t !== clickedTag);
    } else {
        currentBtn.classList.add('selected-tag');
        oppoBtn.classList.add('disabled-tag');
        oppoBtn.classList.remove('selected-tag');
        selectedMainMoods.push(clickedTag);

        // 반대편 태그 해제 시 하위 키워드도 싹 지워줌
        const keywordsToClear = HIERARCHY_DATA_SOURCE[opposingTag];
        selectedSubKeywordsList = selectedSubKeywordsList.filter(item => !keywordsToClear.includes(item));
    }
    syncRenderQ4SubClusterGrid();
}

        // 세부 키워드 그리드 생성 (✨ 이벤트 델리게이션 적용으로 하위 엘리먼트 클릭 씹힘 방지)
        function syncRenderQ4SubClusterGrid() {
            const poolContainer = document.getElementById('q4-dynamic-style-pool');
            if (!poolContainer) return; // 해당 엘리먼트가 없는 페이지라면 스킵
            poolContainer.innerHTML = "";

            if (selectedMainMoods.length === 0) {
                poolContainer.innerHTML = `<div style="grid-column: span 4; text-align: center; color: var(--text-gray); padding: 30px; font-size: 13px;">Q3 무드태그를 선택하시면 상응하는 하위 계층 핵심 이미지셋이 실시간으로 여기에 전개됩니다.</div>`;
                return;
            }

            selectedMainMoods.forEach(clusterKey => {
                const subItems = HIERARCHY_DATA_SOURCE[clusterKey];
                subItems.forEach(keywordValue => {
                    const isChosen = selectedSubKeywordsList.includes(keywordValue);
                    const card = document.createElement('div');
                    card.className = `dynamic-style-card ${isChosen ? 'chosen-style' : ''}`;
                    card.setAttribute('data-keyword', keywordValue);

                    const imageUrl = `/images/${clusterKey.toLowerCase()}_${keywordValue}.png`;
                    console.log("생성된 이미지 URL:", imageUrl);

                    // 포인터 이벤트 논(none)을 주어 내부 프리뷰, 텍스트가 클릭을 흡수하지 못하게 처리
                    card.innerHTML = `
                        <!-- 1. 이미지: 정상 로딩되면 보임 -->
                        <img src="/images/tags/${clusterKey.toLowerCase()}_${keywordValue}.png"
                             style="width: 100%; height: 80px; object-fit: cover; border-radius: 4px; margin-bottom: 8px; display: block;"
                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">

                        <!-- 2. 플레이스홀더: 이미지가 안 뜰 때만 display: flex로 나타남 -->
                        <div class="card-img-placeholder" style="display: none; height: 80px; align-items: center; justify-content: center; background: var(--bg-card-inner); border-radius: 4px; margin-bottom: 8px; font-size: 11px; color: var(--text-gray);">
                            [PREVIEW: ${clusterKey}_${keywordValue}]
                        </div>

                        <div style="text-align:center; font-weight:700; font-size:13px; pointer-events: none;">${keywordValue}</div>
                        <div style="text-align:center; font-size:10px; color:var(--text-gray); margin-top:2px; pointer-events: none;">${clusterKey} 계층 소속</div>
                    `;
                    poolContainer.appendChild(card);
                });
            });

            // 부모 컨테이너에 클릭 이벤트 델리게이션 바인딩
            poolContainer.onclick = function(e) {
                const targetCard = e.target.closest('.dynamic-style-card');
                if (!targetCard) return;
                const kwVal = targetCard.getAttribute('data-keyword');
                if (kwVal) {
                    handleSubCardSelection(targetCard, kwVal);
                }
            };

            enforceQ4LockingLimit();
        }

function handleSubCardSelection(cardElement, keywordValue) {
    if (cardElement.classList.contains('disabled-style')) return;

    if (cardElement.classList.contains('chosen-style')) {
        cardElement.classList.remove('chosen-style');
        selectedSubKeywordsList = selectedSubKeywordsList.filter(k => k !== keywordValue);
    } else {
        if (selectedSubKeywordsList.length >= 4) {
            alert("⚠️ 최대 4개까지만 선택 가능합니다.");
            return;
        }
        cardElement.classList.add('chosen-style');
        selectedSubKeywordsList.push(keywordValue);
    }
    enforceQ4LockingLimit();
}

function enforceQ4LockingLimit() {
    const cards = document.querySelectorAll('.dynamic-style-card');
    cards.forEach(card => {
        const kw = card.getAttribute('data-keyword');
        if (selectedSubKeywordsList.length >= 4 && !selectedSubKeywordsList.includes(kw)) {
            card.classList.add('disabled-style');
        } else {
            card.classList.remove('disabled-style');
        }
    });
}

// ---------------------------------------------------------
// 3. 파이썬 서버 통신 및 페이지 이동 (수정된 핵심 로직)
// ---------------------------------------------------------
function checkInputsAndNavigate() {
    const q1 = document.getElementById('brand-line-summary')?.value.trim();
    const q2 = document.getElementById('brand-object-search')?.value.trim();

    if (!q1 || !q2) {
        alert("⚠️ Q1과 Q2의 내용을 모두 입력해주세요.");
        return;
    }
    if (typeof selectedMainMoods === 'undefined' || selectedMainMoods.length === 0) {
        alert("⚠️ Q3에서 최소 1개의 브랜드 무드태그를 선택해주세요.");
        return;
    }
    if (typeof selectedSubKeywordsList === 'undefined' || selectedSubKeywordsList.length === 0) {
        alert("⚠️ Q4에서 최소 1개의 세부 스타일 키워드를 선택해주세요.");
        return;
    }

    sessionStorage.setItem("brandQ1", q1);
    sessionStorage.setItem("brandQ2", q2);
    sessionStorage.setItem("brandMoods", JSON.stringify(selectedMainMoods));
    sessionStorage.setItem("brandKeywords", JSON.stringify(selectedSubKeywordsList));

    generateAiMoodboard(q1, q2);
}

async function generateAiMoodboard(q1, q2) {
    const submitBtn = document.querySelector('button[onclick="checkInputsAndNavigate()"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerText = "AI 디렉터가 이미지를 발굴 중입니다... (약 10초 소요)";
    }

    try {

        const response = await fetch('/api/ai/moodboard/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                brand_intro: q1,
                item_type: q2,
                selected_tags: selectedSubKeywordsList
            })
        });

        if (!response.ok) throw new Error("서버 에러");

        const data = await response.json();
        sessionStorage.setItem("aiMoodboardData", JSON.stringify(data));
        window.location.href = '/reference';

    } catch (error) {
        console.error("AI 생성 실패:", error);
        alert("파이썬 서버(8000포트)가 켜져있는지 확인해주세요.");
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerText = "레퍼런스 이미지 선택 단계로 이동";
        }
    }
}

// ---------------------------------------------------------
// 4. 레퍼런스 이미지 선택 화면(reference.html) 전용 로직
// ---------------------------------------------------------
function selectSingleReference(card, rowId) {
    const rowContainer = document.getElementById(rowId);
    if (!rowContainer) return;

    if (card.classList.contains('chosen')) {
        card.classList.remove('chosen');
        return;
    }
    if (rowContainer.querySelectorAll('.scroll-card.chosen').length >= 4) {
        alert("⚠️ 최대 4개까지만 선택 가능합니다.");
        return;
    }
    card.classList.add('chosen');
}


        // 캐러셀 페이지 이동 (이전/다음 화살표)
        function moveCarousel(rowId, direction) {
            const wrapper = document.getElementById(`carousel-${rowId}`);
            const pages = wrapper.querySelectorAll('.carousel-page');
            const dots = wrapper.parentElement.querySelectorAll('.dot');

            // 현재 활성화된 페이지 인덱스 찾기
            let currentIndex = Array.from(pages).findIndex(p => p.classList.contains('active'));

            // 이동할 인덱스 계산
            let nextIndex = currentIndex + direction;

            // 범위 체크 (첫 페이지보다 작거나 마지막 페이지보다 크면 무시)
            if (nextIndex < 0 || nextIndex >= pages.length) return;

            // 페이지 전환
            pages[currentIndex].classList.remove('active');
            pages[nextIndex].classList.add('active');

            // 하단 도트 상태 전환
            dots[currentIndex].classList.remove('active');
            dots[nextIndex].classList.add('active');
        }

        // 도트 클릭 시 특정 페이지로 이동
        function goToPage(rowId, pageIndex) {
            const wrapper = document.getElementById(`carousel-${rowId}`);
            const pages = wrapper.querySelectorAll('.carousel-page');
            const dots = wrapper.parentElement.querySelectorAll('.dot');

            // 현재 활성 상태 해제
            wrapper.querySelector('.carousel-page.active').classList.remove('active');
            wrapper.parentElement.querySelector('.dot.active').classList.remove('active');

            // 선택한 페이지 활성
            pages[pageIndex].classList.add('active');
            dots[pageIndex].classList.add('active');
        }


// [brandLogic.js] 내부에 있는 기존 함수를 지우고 이걸로 덮어씌우세요!
function validateAndBuildMoodboard() {
    let organizedDataGlobal = [];
    for (let i = 1; i <= 8; i++) {
        const row = document.getElementById(`carousel-row-${i}`);
        if (!row) continue;
        const selectedCards = row.querySelectorAll('.scroll-card.chosen');
        const catName = row.closest('.category-scroll-block')?.getAttribute('data-cat-name') || `카테고리 ${i}`;

        if (selectedCards.length === 0) { alert(`${catName}에서 이미지를 선택해주세요.`); return; }

        let assets = [];
        selectedCards.forEach(card => {
            const img = card.querySelector('img');
            // 이미지 태그 자체가 아니라 이미지의 URL(주소)만 뽑아서 깔끔하게 저장합니다.
            assets.push(img ? img.src : card.innerText.trim());
        });
        organizedDataGlobal.push({ category: catName, assets: assets });
    }

    // 💡 핵심: 페이지가 넘어가도 기억하도록 브라우저 세션에 선택한 8개 데이터를 저장!
    sessionStorage.setItem("finalMoodboardData", JSON.stringify(organizedDataGlobal));

    // 무드보드 페이지로 이동
    window.location.href = '/moodboard';
}

// ---------------------------------------------------------
// 5. 레퍼런스 페이지 진입 시, 파이썬에서 받은 이미지 화면에 뿌려주기
// ---------------------------------------------------------
window.addEventListener('DOMContentLoaded', () => {
    const savedData = sessionStorage.getItem("aiMoodboardData");

    // 현재 화면이 레퍼런스 화면(carousel-row-1이 존재하는지)인지 확인
    if (savedData && document.getElementById('carousel-row-1')) {
        const data = JSON.parse(savedData);
        const map = {
            'essence': 'carousel-row-1', 'sports': 'carousel-row-2', 'lifestyle': 'carousel-row-3',
            'environment': 'carousel-row-4', 'tone': 'carousel-row-5', 'product': 'carousel-row-6',
            'palette': 'carousel-row-7', 'material': 'carousel-row-8'
        };

        for (const [key, id] of Object.entries(map)) {
            const urls = data.images[key];
            if (!urls) continue;

            const cards = document.getElementById(id)?.querySelectorAll('.scroll-card');
            urls.forEach((url, i) => {
                if (cards && cards[i]) {
                    cards[i].innerHTML = `<img src="${url}" style="width:100%; height:100px; object-fit:cover; border-radius:4px; margin-bottom:6px; display:block;">`;
                }
            });
        }
        // 사용한 데이터는 세션에서 삭제 (새로고침 시 방지)
        sessionStorage.removeItem("aiMoodboardData");
    }
});