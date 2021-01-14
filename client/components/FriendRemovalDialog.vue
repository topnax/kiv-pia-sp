<template>
  <v-dialog
    v-model="display"
    persistent
    max-width="290"
  >
    <v-card>
      <v-card-title class="headline">
        Do you really want to unfriend {{ friendUsername }}?
      </v-card-title>
      <v-card-text>This might hurt {{ friendUsername }}'s feelings...
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          text
          @click="onCancelClicked"
        >
          No
        </v-btn>
        <v-btn
          text
          @click="onConfirmationClicked"
        >
          Yes
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "FriendRemovalDialog",
  props: {
    friendUsername: "",
    onConfirmationClicked: null,
    onCancelClicked: null,
    display: false
  }
}
</script>

<style scoped>

</style>
