// ##### 전역 속성 springai 추가 #####
window.springai = window.springai || {};

// ##### 사용자 질문을 보여줄 엘리먼트를 채팅 패널에 추가하는 함수 #####
springai.addUserQuestion = function (question, chatPanelId) {
  const html = `
    <div class="d-flex justify-content-end m-2">
      <table>
        <tr>
          <td><img src="/image/user.png" width="30"/></td>
          <td><span>${question}</span></td>
        </tr>
      </table>
    </div>
  `;
  document.getElementById(chatPanelId).innerHTML += html;
  springai.scrollToHeight(chatPanelId);
};

// ##### 응답을 보여줄 엘리먼트를 채팅 패널에 추가하는 함수 #####
springai.addAnswerPlaceHolder = function (chatPanelId) {
  //id-를 붙이는 이유: 숫자로 시작하면 CSS 선택자 문법 에러 날 수 있음
  let uuid = "id-" + crypto.randomUUID();
  let html = `
    <div class="d-flex justify-content-start border-bottom m-2">
      <table>
        <tr>
          <td><img src="/image/assistant.png" width="50"/></td>
          <td><span id="${uuid}"></span></td>
        </tr>
      </table>       
    </div>
  `;
  document.getElementById(chatPanelId).innerHTML += html;
  return uuid;
};

// ##### 텍스트 응답을 출력하는 함수 #####
springai.printAnswerText = async function (responseText, targetId, chatPanelId) {
  document.getElementById(targetId).innerHTML = responseText;
  springai.scrollToHeight(chatPanelId);
}

// ##### 스트리밍 텍스트 응답을 출력하는 함수 #####
springai.printStreamingAnswerText = async function (responseBody, targetId, chatPanelId) {
  const targetElement = document.getElementById(targetId);
  const reader = responseBody.getReader();
  const decoder = new TextDecoder("utf-8");
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    const chunk = decoder.decode(value);
    targetElement.innerHTML += chunk;
    springai.scrollToHeight(chatPanelId);
  }
};

// ##### 채팅 패널의 스크롤을 제일 아래로 내려주는 함수 #####
springai.scrollToHeight = function (chatPanelId) {
  //DOM 업데이트보다 스크롤 이동이 먼저 되면 안되므로
  //스크롤 이동을 0.3초간 딜레이 시킴
  setTimeout(() => {
    const chatPanelElement = document.getElementById(chatPanelId);
    chatPanelElement.scrollTop = chatPanelElement.scrollHeight;
  }, 300);
};

// ##### 진행중임을 표시하는 함수 #####
springai.setSpinner = function(spinnerId, status) {
  if(status) {
    document.getElementById("spinner").classList.remove("d-none");
  } else {
    document.getElementById(spinnerId).classList.add("d-none");
  }
} 