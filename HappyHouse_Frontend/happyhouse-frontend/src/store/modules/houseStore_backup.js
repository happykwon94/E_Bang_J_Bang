import http from "@/util/http-common.js";

const houseStore = {
  namespaced: true,
  state: {
    sidos: [{ sidoCode: "", sidoName: "시/도" }],
    guguns: [{ gugunCode: "", gugunName: "구/군" }],
    dongs: [{ dongCode: "", dongName: "동" }],
    houses: null,
    housespos: [],
    houseDeal: null,
    houseInfo: null,
    foodStore: [],
    lifeStore: [],
    eduStore: [],
  },
  mutations: {
    SET_SIDO_LIST(state, data) {
      state.sidos = [{ sidoCode: "", sidoName: "시/도" }];
      data.forEach((sido) => {
        state.sidos.push({ sidoCode: sido.sidoCode, sidoName: sido.sidoName });
      });
      // console.log(state.sidos);
    },
    SET_GUGUN_LIST(state, data) {
      state.guguns = [{ gugunCode: "", gugunName: "구/군" }];
      data.forEach((gugun) => {
        state.guguns.push({
          gugunCode: gugun.gugunCode,
          gugunName: gugun.gugunName,
        });
      });
    },
    SET_DONG_LIST(state, data) {
      state.dongs = [{ dongCode: "", dongName: "동" }];
      data.forEach((dong) => {
        state.dongs.push({ dongCode: dong.dongCode, dongName: dong.dongName });
      });
    },
    SET_APT_LIST(state, data) {
      state.houses = data;
      // state.housespos = [];
      // data.forEach((house) => {
      //   state.housespos.push({ lat: house.lat, lng: house.lng });
      // });
      // // console.log(state.housespos);
    },
    SET_APT_INFO(state, data) {
      state.houseInfo = data;
    },
    SET_APT_DEAL(state, data) {
      state.houseDeal = data;
    },
    SET_FOOD_STORE(state, data) {
      state.foodStore = data;
    },
    SET_LIFE_STORE(state, data) {
      state.lifeStore = data;
    },
    SET_EDU_STORE(state, data) {
      state.eduStore = data;
    },
  },
  actions: {
    getSido({ commit }) {
      http
        .get("/house/sido")
        .then((response) => {
          //   console.log(response);
          // console.log(commit);
          commit("SET_SIDO_LIST", response.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getGugun({ commit }, sidoCode) {
      const params = { sido: sidoCode };
      console.log("시도 :" + sidoCode);
      http
        .get(`/house/gugun`, { params })
        .then((response) => {
          // console.log(response);
          commit("SET_GUGUN_LIST", response.data);
          commit("SET_APT_DEAL", null);
          commit("SET_APT_LIST", []);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getDong({ commit }, gugunCode) {
      console.log("구군: " + gugunCode);
      const params = { gugun: gugunCode };
      http
        .get(`/house/dong`, { params })
        .then((response) => {
          // console.log(response.data);
          commit("SET_DONG_LIST", response.data);
          commit("SET_APT_DEAL", null);
          commit("SET_APT_LIST", []);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getAptList({ commit }, dongCode) {
      console.log("동: " + dongCode);
      const params = { dong: dongCode };
      http
        .get(`/house/aptList`, { params })
        .then((response) => {
          console.log(response.data);
          commit("SET_APT_LIST", response.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getHouse({ commit }, house) {
      console.log("house: " + house);
      const params = { aptCode: house.aptCode };
      http
        .get(`/house/aptInfo`, { params })
        .then((response) => {
          console.log(response.data);
          commit("SET_APT_DEAL", response.data);
          commit("SET_APT_INFO", house);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getStoreList({ commit }, dongName) {
      console.log(dongName);
      console.log(commit);
      var params = { dongName: dongName, classes: "음식" };
      http.get(`/house/store`, { params }).then((response) => {
        commit("SET_FOOD_STORE", response.data);
      });
      params = { dongName: dongName, classes: "소매" };
      http.get(`/house/store`, { params }).then((response) => {
        commit("SET_LIFE_STORE", response.data);
      });
      params = { dongName: dongName, classes: "학문/교육" };
      http.get(`/house/store`, { params }).then((response) => {
        commit("SET_EDU_STORE", response.data);
      });
    },
  },
  modules: {},
};

export default houseStore;